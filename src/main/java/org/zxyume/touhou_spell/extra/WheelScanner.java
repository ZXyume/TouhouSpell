package org.zxyume.touhou_spell.extra;

import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager.SelectionOption;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.api.spells.SpellSlot;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/**
 * 单点实现「当前法术轮盘里有哪些法术、各自的基础等级」。
 *
 * <p>规则与 {@code SpellSelectionManager#initItem} 对齐：只看 {@link ISpellContainer#isSpellWheel()}
 * 的容器；{@code mustEquip()} 的容器拿在手上不算；同一法术出现在多处时取**最高**等级。
 * {@code SpellSelectionManager} 作为补充来源合并进来。
 *
 * <p>1.4.1 起快照带 {@code reliable} 标志：只有在数据来源可用、且没有异常时才算"可信"。
 * 调用方（{@code GrimoireRestrictionHandler}）凭这个标志决定能否依据"空轮盘"清理效果 ——
 * 否则"卸下手册 → 空轮盘"会被误判成"扫描失败"，四个效果永远清不掉。
 */
public final class WheelScanner {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final int CACHE_TICKS = 10;
   private static final EquipmentSlot[] SLOTS = {
      EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.HEAD,
      EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
   };
   private static final String MAINHAND = "mainhand";
   private static final String OFFHAND = "offhand";
   private static final Map<UUID, Snapshot> CACHE = new ConcurrentHashMap<>();

   private WheelScanner() {
   }

   /**
    * 轮盘快照。
    *
    * @param ids      轮盘里可达的法术 id
    * @param levels   每个法术的基础等级
    * @param until    缓存到期 tick
    * @param reliable 本次扫描是否可信（false = 数据来源不可用/出过异常，不能据此清理效果）
    */
   public record Snapshot(Set<String> ids, Map<ResourceLocation, Integer> levels, long until, boolean reliable) {
   }

   /** 轮盘里可达的法术 id 集合。 */
   public static Set<String> spellIds(Player player) {
      return snapshot(player).ids();
   }

   /** 轮盘里每个法术的基础等级（法术 -> 等级）。 */
   public static Map<ResourceLocation, Integer> baseLevels(Player player) {
      return snapshot(player).levels();
   }

   /** 该法术在轮盘里的基础等级；不在轮盘里返回 -1。 */
   public static int baseLevel(Player player, ResourceLocation spellId) {
      Integer level = baseLevels(player).get(spellId);
      return level == null ? -1 : level;
   }

   /** 清掉某个玩家的缓存（登出、切维度、重生）。 */
   public static void forgetPlayer(UUID id) {
      if (id != null) {
         CACHE.remove(id);
      }
   }

   public static Snapshot snapshot(Player player) {
      UUID id = player.m_20148_();
      long now = player.f_19797_;
      Snapshot cached = CACHE.get(id);
      if (cached != null && now < cached.until()) {
         return cached;
      }

      Set<String> ids = new HashSet<>();
      Map<ResourceLocation, Integer> levels = new HashMap<>();
      boolean[] reliable = {true};

      collectCurios(player, ids, levels, reliable);
      for (EquipmentSlot slot : SLOTS) {
         collect(player.m_6844_(slot), slotName(slot), ids, levels, reliable);
      }

      try {
         for (SelectionOption option : new SpellSelectionManager(player).getAllSpells()) {
            SpellData data = option.spellData;
            if (data != null && data.getSpell() != null) {
               ids.add(data.getSpell().getSpellId());
            }
         }
      } catch (Throwable throwable) {
         reliable[0] = false;
         LOGGER.debug("[touhou_spell] SpellSelectionManager scan failed", throwable);
      }

      Snapshot snapshot = new Snapshot(Set.copyOf(ids), Map.copyOf(levels), now + CACHE_TICKS, reliable[0]);
      CACHE.put(id, snapshot);
      return snapshot;
   }

   private static String slotName(EquipmentSlot slot) {
      return switch (slot) {
         case MAINHAND -> MAINHAND;
         case OFFHAND -> OFFHAND;
         default -> slot.m_20751_();
      };
   }

   private static void collectCurios(Player player, Set<String> ids, Map<ResourceLocation, Integer> levels, boolean[] reliable) {
      try {
         Optional<ICuriosItemHandler> inventory = CuriosApi.getCuriosInventory(player).resolve();
         if (inventory.isEmpty()) {
            // curios 能力还没挂上（刚登录/刚传送）：这一次的结论不可信
            reliable[0] = false;
            return;
         }

         for (Map.Entry<String, ICurioStacksHandler> entry : inventory.get().getCurios().entrySet()) {
            ICurioStacksHandler handler = entry.getValue();
            if (handler == null) {
               continue;
            }

            int slots = handler.getStacks().getSlots();
            for (int i = 0; i < slots; i++) {
               collect(handler.getStacks().getStackInSlot(i), entry.getKey(), ids, levels, reliable);
            }
         }
      } catch (Throwable throwable) {
         reliable[0] = false;
         LOGGER.debug("[touhou_spell] curio scan failed", throwable);
      }
   }

   private static void collect(ItemStack stack, String slot, Set<String> ids, Map<ResourceLocation, Integer> levels, boolean[] reliable) {
      if (stack == null || stack.m_41619_()) {
         return;
      }

      try {
         if (!ISpellContainer.isSpellContainer(stack)) {
            return;
         }

         ISpellContainer container = ISpellContainer.get(stack);
         if (container == null || !container.isSpellWheel()) {
            return;
         }

         if (container.mustEquip() && (MAINHAND.equals(slot) || OFFHAND.equals(slot))) {
            return;
         }

         for (SpellSlot spellSlot : container.getActiveSpells()) {
            if (spellSlot == null) {
               continue;
            }

            SpellData data = spellSlot.spellData();
            if (data == null || data.getSpell() == null) {
               continue;
            }

            ids.add(data.getSpell().getSpellId());
            ResourceLocation spellId = data.getSpell().getSpellResource();
            int level = data.getLevel();
            Integer existing = levels.get(spellId);
            if (existing == null || level > existing) {
               levels.put(spellId, level);
            }
         }
      } catch (Throwable throwable) {
         reliable[0] = false;
         LOGGER.debug("[touhou_spell] spell container scan failed", throwable);
      }
   }
}

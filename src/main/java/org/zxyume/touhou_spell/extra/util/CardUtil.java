package org.zxyume.touhou_spell.extra.util;

import dev.xkmc.l2library.util.raytrace.RayTraceUtil;
import dev.xkmc.l2library.util.raytrace.RayTraceUtil.ServerTarget;
import dev.xkmc.youkaishomecoming.content.item.danmaku.ISpellItem;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.zxyume.touhou_spell.extra.SpellCardSettings;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public final class CardUtil {
   public static final String SLOT = "spell_card";
   private static final double TARGET_RANGE = 64.0;
   private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
   /** 每个符卡类的 requireTarget 字段（反射只做一次）。 */
   private static final Map<Class<?>, Boolean> REQUIRE_TARGET_CACHE = new ConcurrentHashMap<>();

   private CardUtil() {
   }

   public static ItemStack getCard(Player var0) {
      return CuriosApi.getCuriosInventory(var0).map(var0x -> {
         ICurioStacksHandler var1 = (ICurioStacksHandler)var0x.getCurios().get("spell_card");
         return var1 == null ? ItemStack.f_41583_ : var1.getStacks().getStackInSlot(0);
      }).orElse(ItemStack.f_41583_);
   }

   public static boolean hasCard(Player var0) {
      if (var0 == null) {
         return false;
      } else {
         ItemStack var1 = getCard(var0);
         return !var1.m_41619_() && var1.m_41720_() instanceof ISpellItem;
      }
   }

   public static boolean isOnCooldown(Player var0, Item var1) {
      return var0.m_36335_().m_41519_(var1);
   }

   public static void addCooldown(Player var0, Item var1, double var2) {
      ResourceLocation var4 = ForgeRegistries.ITEMS.getKey(var1);
      int var5 = var4 == null ? 200 : SpellCardSettings.cooldownFor(var4.toString());
      var5 = (int)Math.round((double)var5 * var2);
      var0.m_36335_().m_41524_(var1, Math.max(1, var5));
   }

   public static boolean release(ServerPlayer var0) {
      ItemStack var1 = getCard(var0);
      if (var1.m_41619_()) {
         var0.m_5661_(Component.m_237115_("message.touhou_spell.no_card"), true);
         return false;
      } else {
         Item var2 = var1.m_41720_();
         if (var2 instanceof ISpellItem var3) {
            if (isOnCooldown(var0, var2)) {
               var0.m_5661_(Component.m_237115_("message.touhou_spell.cooldown"), true);
               return false;
            } else {
               boolean var4 = requiresTarget(var2);
               UUID var5 = var0.m_20148_();
               if (var4) {
                  RayTraceUtil.TARGET_MAP.remove(var5);
                  EntityHitResult var6 = RayTraceUtil.rayTraceEntity(var0, TARGET_RANGE, var1x -> var1x instanceof LivingEntity && var1x != var0);
                  if (var6 != null) {
                     RayTraceUtil.TARGET_MAP.put(var5, new ServerTarget(var6.m_82443_().m_20148_()));
                  }
               }

               boolean var13;
               try {
                  var13 = var3.castSpell(var1, var0, false, false);
               } catch (Throwable var11) {
                  LOGGER.debug("[touhou_spell] spell card cast failed", var11);
                  var13 = false;
               } finally {
                  if (var4) {
                     RayTraceUtil.TARGET_MAP.remove(var5);
                  }
               }

               if (!var13) {
                  var0.m_5661_(Component.m_237115_("message.touhou_spell.failed"), true);
                  return false;
               } else {
                  addCooldown(var0, var2, 1.0);
                  return true;
               }
            }
         } else {
            var0.m_5661_(Component.m_237115_("message.touhou_spell.not_spell_card"), true);
            return false;
         }
      }
   }

   /** True when casting this card needs a looked-at entity, so it cannot be auto-cast on its own. */
   public static boolean requiresTarget(ItemStack var0) {
      return var0.m_41619_() ? false : requiresTarget(var0.m_41720_());
   }

   private static boolean requiresTarget(Item item) {
      return REQUIRE_TARGET_CACHE.computeIfAbsent(item.getClass(), CardUtil::readRequireTarget);
   }

   /** 反射读 YHC 的私有 {@code requireTarget}；找不到字段时按"需要目标"处理（保守）。 */
   private static boolean readRequireTarget(Class<?> type) {
      Class<?> current = type;
      while (current != null && current != Object.class) {
         try {
            Field field = current.getDeclaredField("requireTarget");
            field.setAccessible(true);
            return field.getBoolean(null);
         } catch (NoSuchFieldException missing) {
            current = current.getSuperclass();
         } catch (Throwable throwable) {
            LOGGER.debug("[touhou_spell] could not read requireTarget from {}", type.getName(), throwable);
            return true;
         }
      }

      LOGGER.debug("[touhou_spell] {} has no requireTarget field, assuming it needs a target", type.getName());
      return true;
   }
}

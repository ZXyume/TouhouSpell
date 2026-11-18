package org.zxyume.touhou_spell.capabilities;

import dev.xkmc.l2library.util.raytrace.RayTraceUtil;
import dev.xkmc.youkaishomecoming.content.capability.GrazeHelper;
import dev.xkmc.youkaishomecoming.content.entity.danmaku.ItemDanmakuEntity;
import dev.xkmc.youkaishomecoming.content.entity.danmaku.ItemLaserEntity;
import dev.xkmc.youkaishomecoming.content.item.danmaku.DanmakuItem;
import dev.xkmc.youkaishomecoming.content.item.danmaku.LaserItem;
import dev.xkmc.youkaishomecoming.content.spell.item.SpellContainer;
import dev.xkmc.youkaishomecoming.init.data.YHModConfig;
import dev.xkmc.youkaishomecoming.init.registrate.YHDanmaku.Laser;
import dev.xkmc.youkaishomecoming.init.registrate.YHEntities;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.zxyume.touhou_spell.extra.PowerPoints;
import org.zxyume.touhou_spell.extra.SpellMath;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;
import org.zxyume.touhou_spell.extra.WheelLevels;
import org.zxyume.touhou_spell.init.ModAttribute;
import org.zxyume.touhou_spell.init.ModSpell;
import org.zxyume.touhou_spell.util.DanmakuCategory;
import org.zxyume.touhou_spell.util.DanmakuMath;

/**
 * 弹种存储（0 = 主炮、1 = 子机）与"一次性齐射"的发射管线（2.0.0）。
 *
 * <p>2.0.0 的规则：
 *
 * <ol>
 *   <li>一次右键：主炮 1 发 + 子机 N 枚<b>同刻</b>发射；子机数量 = {@code 1 + 火力全开等级}（封顶 7）；</li>
 *   <li>扣蓝顺序：先主炮（不足则跳过主炮），再子机（不足则只发 1 枚；仍不足则跳过子机）；</li>
 *   <li>0 tick 一次都没发出去 → 不进冷却、也不排程；</li>
 *   <li>冷却与额外发射由「主炮 / 子机分别是 A（非激光，含短激光、空槽）还是 B（激光）」决定；
 *       激光槽位这一轮实际发射 0 个时，本次按 A 处理（2.1.2）：</li>
 * </ol>
 *
 * <table>
 *   <tr><th>主炮</th><th>子机</th><th>冷却</th><th>额外发射</th></tr>
 *   <tr><td>A</td><td>A</td><td>20t</td><td>无</td></tr>
 *   <tr><td>A</td><td>B</td><td>40t</td><td>主炮 @20t</td></tr>
 *   <tr><td>B</td><td>A</td><td>80t</td><td>子机 @20/40/60t</td></tr>
 *   <tr><td>B</td><td>B</td><td>80t</td><td>子机 @40t</td></tr>
 * </table>
 *
 * <p>弹种、等级派生数值、火力提升状态在 0 tick 全部快照，所以发射途中换弹/换戒指不影响这一轮；
 * 只有法力在**每一次发射触发时**结算。
 */
public class DanmakuHandler extends ItemStackHandler {
   public static final int MAIN_SLOT = 0;
   public static final int OPTION_SLOT = 1;
   /** A,A：非激光主炮 + 非激光子机。 */
   public static final int COOLDOWN_AA = 20;
   /** A,B：非激光主炮 + 激光子机（主炮额外一次）。 */
   public static final int COOLDOWN_AB = 40;
   /** B,A：激光主炮 + 非激光子机（子机额外三次）。 */
   public static final int COOLDOWN_BA = 80;
   /** B,B：双激光（子机额外一次）。 */
   public static final int COOLDOWN_BB = 80;

   /** 2.1.23：火力提升不再额外耗蓝；P 点改为按本次法力消耗的固定比例扣除（2.1.24 起为 0.3%）。 */
   private static final double POWER_COST_PER_MANA = 0.003;
   /** 2.1.4：子机发射结算时的耗蓝系数（消耗减半）。 */
   private static final double OPTION_FIRE_COST_FACTOR = 0.5;

   /** 法力比较的容差（float → double）。 */
   private static final double MANA_EPSILON = 1.0E-4;

   /** 每个玩家尚未打完的射击队列。 */
   private static final Map<UUID, Deque<Scheduled>> QUEUE = new ConcurrentHashMap<>();

   /**
    * 一次排定的射击。
    *
    * @param due       触发时刻（{@code level.getGameTime()}）
    * @param option    true = 子机齐射，false = 主炮
    * @param ammo      0 tick 快照下来的弹种（中途换弹不影响）
    * @param count     本次发射的枚数（子机用，0 tick 快照）
    * @param damageMul 已经算好的伤害倍率（含等级加成与火力提升）
    * @param powered  0 tick 快照：本轮齐射是否处于火力提升状态（决定要不要扣 P 点）
    */
   private record Scheduled(long due, boolean option, ItemStack ammo, int count, float damageMul, boolean powered) {
   }

   public DanmakuHandler() {
      super(2);
   }

   public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
      Item item = stack.m_41720_();
      return item instanceof DanmakuItem || item instanceof LaserItem;
   }

   /** The ammo slots are a single-item selector, not a stack storage. */
   public int getSlotLimit(int slot) {
      return 1;
   }

   protected int getStackLimit(int slot, @NotNull ItemStack stack) {
      return 1;
   }

   // ------------------------------------------------------------------ 齐射

   /**
    * 触发一次齐射。
    *
    * @return 冷却 tick 数；{@code 0} 表示一次都没发射（不进入冷却）
    */
   public int fireVolley(ServerPlayer player, MagicData magicData) {
      ItemStack main = this.getStackInSlot(MAIN_SLOT);
      ItemStack option = this.getStackInSlot(OPTION_SLOT);
      if (main.m_41619_() && option.m_41619_()) {
         return 0;
      }

      if (GrazeHelper.forbidDanmaku(player)) {
         return 0;
      }

      if (magicData == null) {
         return 0;
      }

      // ---- 0 tick 快照：弹种、等级派生数值、火力提升状态
      boolean powered = player.m_21023_(TouhouSpellExtra.POWER_UP_EFFECT);
      // 2.1.3：火力提升的效果与额外耗蓝都按 P 点比例缩放
      boolean poweredShot = powered;
      int powerAmp = powered
         ? (int)Math.floor(SpellMath.percentForLevel(WheelLevels.activeLevel(player, TouhouSpellExtra.POWER_UP_SPELL), player))
         : 0;
      int level = player.m_21023_(TouhouSpellExtra.MULTI_EFFECT) ? WheelLevels.activeLevel(player, ModSpell.TEARS_UP.get()) : 0;
      int extraOptions = Math.max(0, (int)Math.round(player.m_21133_((Attribute)ModAttribute.D_OPTION.get())) - 1);

      float mainMul = (float)(SpellMath.mainDamageMultiplier(level) * damageBoost(powerAmp));
      float optionMul = (float)(0.5 * damageBoost(powerAmp));
      int optionCount = option.m_41619_() ? 0 : SpellMath.optionCount(level, extraOptions);

      boolean mainLaserRaw = isLongLaser(main);
      boolean optionLaserRaw = isLongLaser(option);

      double mainCost = bulletCost(main, 1.0);
      double optionCost = bulletCost(option, 1.0) * OPTION_FIRE_COST_FACTOR;

      double mana = magicData.getMana();
      boolean mainFired = false;
      int optionFired = 0;

      // ---- 主炮（0 tick）
      if (!main.m_41619_() && mana + MANA_EPSILON >= mainCost) {
         magicData.addMana((float)(-mainCost));
         mana -= mainCost;
         shootMain(player, main, mainMul);
         consumePower(player, poweredShot, mainCost);
         mainFired = true;
      }

      // ---- 子机（0 tick）
      if (!option.m_41619_() && optionCount > 0) {
         if (mana + MANA_EPSILON >= optionCost * (double)optionCount) {
            magicData.addMana((float)(-optionCost * (double)optionCount));
            mana -= optionCost * (double)optionCount;
            shootOptions(player, option, optionMul, optionCount);
            consumePower(player, poweredShot, optionCost * (double)optionCount);
            optionFired = optionCount;
         } else if (mana + MANA_EPSILON >= optionCost) {
            magicData.addMana((float)(-optionCost));
            shootOptions(player, option, optionMul, 1);
            consumePower(player, poweredShot, optionCost);
            optionFired = 1;
         }
      }

      if (!mainFired && optionFired <= 0) {
         // 一发都没打出去：不进入冷却，也不排程
         return 0;
      }

      // 2.1.2：激光槽位这一轮实际发射 0 个（蓝量不足被跳过）→ 本次按普通弹幕（A）处理
      boolean mainLaser = mainLaserRaw && mainFired;
      boolean optionLaser = optionLaserRaw && optionFired > 0;
      int cooldown = cooldownFor(mainLaser, optionLaser);

      // 2.0.3：与 YHC 手持弹幕使用同一个发射音效（0 tick 只播一次，不随发射枚数叠加）
      playFireSound(player);

      // ---- 额外发射（法力在各自触发时结算）
      Deque<Scheduled> queue = queueOf(player);
      long now = player.m_9236_().m_46467_();
      for (int offset : extraMainTicks(mainLaser, optionLaser)) {
         if (!main.m_41619_()) {
            queue.add(new Scheduled(now + (long)offset, false, main.m_41777_(), 1, mainMul, poweredShot));
         }
      }

      for (int offset : extraOptionTicks(mainLaser, optionLaser)) {
         if (!option.m_41619_() && optionCount > 0) {
            queue.add(new Scheduled(now + (long)offset, true, option.m_41777_(), optionCount, optionMul, poweredShot));
         }
      }

      // 2.1.6：没有额外发射时不要把空队列留在 QUEUE 里（否则要等到登出才回收）
      if (queue.isEmpty()) {
         QUEUE.remove(player.m_20148_());
      }

      return cooldown;
   }

   /** 额外的主炮时刻（不含 0 tick）。 */
   private static int[] extraMainTicks(boolean mainLaser, boolean optionLaser) {
      // A,B：主炮在释放后 1s 再打一次
      return !mainLaser && optionLaser ? new int[]{20} : new int[0];
   }

   /** 额外的子机时刻（不含 0 tick）。 */
   private static int[] extraOptionTicks(boolean mainLaser, boolean optionLaser) {
      if (!mainLaser) {
         return new int[0];
      }

      // B,B：释放后 2s 再齐射一次；B,A：释放后 1s / 2s / 3s 各一次
      return optionLaser ? new int[]{40} : new int[]{20, 40, 60};
   }

   /**
    * 推进队列（由 {@code CapabilityEvent} 每 tick 调用）。
    *
    * <p>每一条排定的射击都在**触发当刻**结算自己的法力；付不起就跳过。
    * 死亡 / 被禁射时清空；万花币离手<b>不</b>取消（2.0.0）。
    */
   public void tickQueue(ServerPlayer player, MagicData magicData) {
      UUID id = player.m_20148_();
      Deque<Scheduled> queue = QUEUE.get(id);
      if (queue == null || queue.isEmpty()) {
         return;
      }

      if (!player.m_6084_() || GrazeHelper.forbidDanmaku(player)) {
         clearQueue(id);
         return;
      }

      long now = player.m_9236_().m_46467_();
      List<Scheduled> due = new ArrayList<>(queue.size());
      Iterator<Scheduled> iterator = queue.iterator();
      while (iterator.hasNext()) {
         Scheduled shot = iterator.next();
         if (shot.due() <= now) {
            due.add(shot);
            iterator.remove();
         }
      }

      for (Scheduled shot : due) {
         fireScheduled(player, magicData, shot);
      }

      if (queue.isEmpty()) {
         QUEUE.remove(id);
      }
   }

   /** 触发一次排定的射击：法力在这一刻结算，付不起就跳过。 */
   private void fireScheduled(ServerPlayer player, MagicData magicData, Scheduled shot) {
      double cost = bulletCost(shot.ammo(), 1.0) * (shot.option() ? OPTION_FIRE_COST_FACTOR : 1.0);
      double mana = magicData.getMana();

      if (shot.option()) {
         int count = shot.count();
         if (count > 0 && mana + MANA_EPSILON >= cost * (double)count) {
            magicData.addMana((float)(-cost * (double)count));
            shootOptions(player, shot.ammo(), shot.damageMul(), count);
            playFireSound(player);
            consumePower(player, shot.powered(), cost * (double)count);
         } else if (mana + MANA_EPSILON >= cost) {
            magicData.addMana((float)(-cost));
            shootOptions(player, shot.ammo(), shot.damageMul(), 1);
            playFireSound(player);
            consumePower(player, shot.powered(), cost);
         }

         return;
      }

      if (mana + MANA_EPSILON >= cost) {
         magicData.addMana((float)(-cost));
         shootMain(player, shot.ammo(), shot.damageMul());
         playFireSound(player);
         consumePower(player, shot.powered(), cost);
      }
   }

   public static void clearQueue(UUID id) {
      if (id != null) {
         QUEUE.remove(id);
      }
   }

   public static boolean hasPending(UUID id) {
      Deque<Scheduled> queue = id == null ? null : QUEUE.get(id);
      return queue != null && !queue.isEmpty();
   }

   private static Deque<Scheduled> queueOf(Player player) {
      return QUEUE.computeIfAbsent(player.m_20148_(), key -> new ArrayDeque<>());
   }

   // ------------------------------------------------------------------ 规则

   /** 冷却：只有"长激光"算 B；{@code A} = 非激光（含短激光与空槽）。 */
   public static int cooldownFor(boolean mainLaser, boolean optionLaser) {
      if (mainLaser) {
         return optionLaser ? COOLDOWN_BB : COOLDOWN_BA;
      }

      return optionLaser ? COOLDOWN_AB : COOLDOWN_AA;
   }

   /** 是否为"长激光"（短激光 PENCIL 按普通弹幕处理）。 */
   public static boolean isLongLaser(ItemStack stack) {
      return stack.m_41720_() instanceof LaserItem item && item.type != Laser.PENCIL;
   }

   /**
    * 单发法力（2.0.5：弹幕与短激光翻倍）。
    *
    * <p>弹幕 / 短激光 = 自身伤害值 × 2；长激光固定 {@link DanmakuCategory#LASER_MANA_COST}。与槽位无关。
    */
   public static double bulletCost(ItemStack stack, double multiplier) {
      Item item = stack.m_41720_();
      double base;
      if (item instanceof LaserItem laser) {
         base = laser.type == Laser.PENCIL ? (double)laser.type.damage() * 2.0 : DanmakuCategory.LASER_MANA_COST;
      } else if (item instanceof DanmakuItem danmaku) {
         base = (double)danmaku.type.damage() * 2.0;
      } else {
         return 0.0;
      }

      return base * multiplier;
   }

   /** 2.0.3：与 YHC 的 {@code DanmakuItem#use} 完全一致的发射音效。 */
   /**
    * 2.1.23/2.1.24：火力提升生效时扣 P 点 —— 固定扣除「本次实际消耗法力 × 0.003」。
    *
    * <p>只有真正打出去才调用；未开火力提升（或本发没花法力）时什么都不做。
    *
    * @param powered  0 tick 快照的火力提升状态
    * @param manaSpent 这一发（或这一批子机）实际扣掉的法力
    */
   private static void consumePower(ServerPlayer player, boolean powered, double manaSpent) {
      if (!powered || manaSpent <= 0.0) {
         return;
      }

      PowerPoints.consumeFlat(player, manaSpent * POWER_COST_PER_MANA);
   }

   private static void playFireSound(ServerPlayer player) {
      Level level = player.m_9236_();
      level.m_6263_(
         null,
         player.m_20185_(),
         player.m_20186_(),
         player.m_20189_(),
         SoundEvents.f_12473_,
         SoundSource.PLAYERS,
         0.5F,
         1.0F / (level.m_213780_().m_188501_() * 0.4F + 0.8F)
      );
   }

   /** 火力提升的增伤倍率（0 级 = 1.0）。 */
   private static double damageBoost(int powerAmp) {
      return 1.0 + (double)Math.max(0, powerAmp) / 100.0;
   }

   // ------------------------------------------------------------------ 生成弹幕

   private void shootMain(ServerPlayer player, ItemStack stack, float damageMul) {
      if (stack.m_41619_()) {
         return;
      }

      Vec3 pos = player.m_146892_();
      if (stack.m_41720_() instanceof DanmakuItem item) {
         this.spawnProjectile(player, stack, item, pos, damageMul);
      } else if (stack.m_41720_() instanceof LaserItem item) {
         this.spawnLaser(player, stack, item, pos, damageMul);
      }
   }

   private void shootOptions(ServerPlayer player, ItemStack stack, float damageMul, int count) {
      if (stack.m_41619_() || count <= 0) {
         return;
      }

      int total = Math.max(1, count);
      if (stack.m_41720_() instanceof DanmakuItem item) {
         for (int i = 0; i < total; i++) {
            this.spawnProjectile(player, stack, item, DanmakuMath.getOptionPos(player, i, total, player.f_19797_, 1.0F), damageMul);
         }
      } else if (stack.m_41720_() instanceof LaserItem item) {
         for (int i = 0; i < total; i++) {
            this.spawnLaser(player, stack, item, DanmakuMath.getOptionPos(player, i, total, player.f_19797_, 1.0F), damageMul);
         }
      }
   }

   /** 统一伤害口径：基础伤害 × 倍率 × 法术强度 + danmaku_power（弹幕学强度由 YHC 结算，见 SpellMath#multiplier）。 */
   private static float danmakuDamage(Player player, float baseDamage, float damageMul) {
      return (float)((double)(baseDamage * damageMul) * SpellMath.multiplier(player))
         + (float)player.m_21133_((Attribute)ModAttribute.D_POWER.get());
   }

   private void spawnProjectile(ServerPlayer player, ItemStack stack, DanmakuItem item, Vec3 pos, float damageMul) {
      DanmakuCategory cat = DanmakuCategory.fromBullet(item.type);
      Level level = player.m_9236_();
      ItemDanmakuEntity danmaku = new ItemDanmakuEntity((EntityType)YHEntities.ITEM_DANMAKU.get(), player, level);
      danmaku.setItem(stack.m_41777_());
      danmaku.m_6034_(pos.f_82479_, pos.f_82480_, pos.f_82481_);
      float finalDamage = danmakuDamage(player, (float)item.type.damage(), damageMul);
      float finalSpeed = 1.5F * cat.speedMod;
      danmaku.setup(
         finalDamage, 40, false, item.type.bypass(), RayTraceUtil.getRayTerm(Vec3.f_82478_, player.m_146909_(), player.m_146908_(), (double)finalSpeed)
      );
      level.m_7967_(danmaku);
      SpellContainer.track(player, danmaku);
   }

   private void spawnLaser(ServerPlayer player, ItemStack stack, LaserItem item, Vec3 pos, float damageMul) {
      DanmakuCategory cat = DanmakuCategory.fromLaser(item.type);
      Level level = player.m_9236_();
      ItemLaserEntity laser = new ItemLaserEntity((EntityType)YHEntities.ITEM_LASER.get(), player, level);
      laser.setItem(stack.m_41777_());
      laser.m_6034_(pos.f_82479_, pos.f_82480_, pos.f_82481_);
      float finalDamage = danmakuDamage(player, (float)item.type.damage(), damageMul);
      int dur = (Integer)YHModConfig.COMMON.playerLaserDuration.get();
      if (item.type.setupLength()) {
         int delay = 4;
         float v = 2.0F * cat.speedMod;
         float lenAll = v * (float)delay;
         float vl = item.type.visualLength();
         float len = lenAll / vl;
         float v0 = (vl - 1.0F) / 2.0F * v;
         laser.setup(finalDamage, dur, len, false, player.m_146908_(), player.m_146909_());
         laser.setupLength = true;
         laser.setupTime(1, delay, dur, 1);
         laser.setDelayedMover(v0, v, 1, delay);
      } else {
         laser.setup(finalDamage, dur, 40.0F, false, player.m_146908_(), player.m_146909_());
      }

      level.m_7967_(laser);
      SpellContainer.track(player, laser);
   }
}

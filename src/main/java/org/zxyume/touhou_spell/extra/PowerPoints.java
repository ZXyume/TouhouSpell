package org.zxyume.touhou_spell.extra;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

/**
 * 车万女仆（{@code touhou_little_maid}）提供的 P 点读取（2.1.3）。
 *
 * <p>TLM 把 P 点做成 Forge 能力 {@code PowerCapabilityProvider.POWER_CAP}（0 ~ {@link #MAX}，
 * 服务端权威并同步到客户端）。这里做了两层隔离，未安装 TLM 或能力缺失时一律返回 0：
 *
 * <ul>
 *   <li>{@link ModList#isLoaded(String)} 守卫，避免 TLM 不在时触发类加载；</li>
 *   <li>真正接触 TLM 类型的代码放在内部类 {@link Holder} 里，外面只 catch {@link Throwable}。</li>
 * </ul>
 */
public final class PowerPoints {
   /** TLM 的 P 点上限（{@code PowerCapability.MAX_POWER}）。 */
   public static final float MAX = 5.0F;
   private static final String MOD_ID = "touhou_little_maid";

   private PowerPoints() {
   }

   /** 玩家当前 P 点（0 ~ 5）；未安装 TLM 时为 0。 */
   public static float of(LivingEntity entity) {
      if (entity == null || !ModList.get().isLoaded(MOD_ID)) {
         return 0.0F;
      }

      try {
         return Math.max(0.0F, Math.min(MAX, Holder.read(entity)));
      } catch (Throwable throwable) {
         return 0.0F;
      }
   }

   /** P 点比例（0 ~ 1），用于按比例缩放的倍率。 */
   public static double ratio(LivingEntity entity) {
      return (double)of(entity) / (double)MAX;
   }

   /**
    * 按"当前值"的比例消耗 P 点（2.1.5）。
    *
    * @param rate  每次消耗的比例（0~1），例如 0.02 = 2%
    * @param times 连续结算次数（激光 = 4）
    * @return 消耗后的 P 点
    */
   public static float consume(LivingEntity entity, double rate, int times) {
      if (entity == null || times <= 0 || rate <= 0.0 || !ModList.get().isLoaded(MOD_ID)) {
         return of(entity);
      }

      try {
         return Holder.consume(entity, rate, times);
      } catch (Throwable throwable) {
         return 0.0F;
      }
   }

   /**
            * 2.1.23：按**固定点数**扣 P 点（本次法力消耗 × 0.01 的规则用它）。
            *
            * @param amount 要扣掉的点数（0 ~ MAX）
            * @return 扣完后的 P 点
            */
   public static float consumeFlat(LivingEntity entity, double amount) {
      if (entity == null || amount <= 0.0 || !ModList.get().isLoaded(MOD_ID)) {
         return of(entity);
      }

      try {
         return Holder.consumeFlat(entity, amount);
      } catch (Throwable throwable) {
         return 0.0F;
      }
   }

   /** 隔离 TLM 类型：只有真的装了 TLM 才会加载这个类。 */
   private static final class Holder {
      private static float consume(LivingEntity entity, double rate, int times) {
         return (Float)entity.getCapability(
               com.github.tartaricacid.touhoulittlemaid.capability.PowerCapabilityProvider.POWER_CAP
            )
            .map(power -> {
               double factor = Math.pow(1.0 - Math.min(1.0, rate), times);
               float next = (float)Math.max(0.0, Math.min(MAX, (double)power.get() * factor));
               power.set(next);
               power.markDirty();
               return next;
            })
            .orElse(0.0F);
      }

      private static float consumeFlat(LivingEntity entity, double amount) {
         return (Float)entity.getCapability(
               com.github.tartaricacid.touhoulittlemaid.capability.PowerCapabilityProvider.POWER_CAP
            )
            .map(power -> {
               float next = (float)Math.max(0.0, Math.min((double)MAX, (double)power.get() - amount));
               power.set(next);
               power.markDirty();
               return next;
            })
            .orElse(0.0F);
      }

      private static float read(LivingEntity entity) {
         return (Float)entity.getCapability(
               com.github.tartaricacid.touhoulittlemaid.capability.PowerCapabilityProvider.POWER_CAP
            )
            .map(com.github.tartaricacid.touhoulittlemaid.capability.PowerCapability::get)
            .orElse(0.0F);
      }
   }
}

package org.zxyume.touhou_spell.extra.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 火力全开（2.0.0 合并了原「多重弹幕」）的状态效果。
 *
 * <p>效果只作"开 / 关"标志（amplifier 恒 0）；子机数量与伤害在发射时从轮盘等级现读，
 * 所以这里不再挂任何属性修饰符（{@code danmaku_option_count} 只作为后续装备的加值端口）。
 */
public class MultiDanmakuEffect extends MobEffect {
   public MultiDanmakuEffect() {
      super(MobEffectCategory.BENEFICIAL, 16726843);
   }
}

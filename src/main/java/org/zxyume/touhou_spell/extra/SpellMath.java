package org.zxyume.touhou_spell.extra;

import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 弹幕学的数值换算（单一来源）。
 *
 * <p>2.0.0 起"火力全开"与"多重弹幕"合并成一条法术（id 仍是 {@code full_firepower}）：
 *
 * <ul>
 *   <li>{@link #optionCount(int, int)}：子机数量 = {@code 1 + 有效等级 + 加值}（2.1.26 起无上限，只保底 1）；</li>
 *   <li>{@link #mainDamageMultiplier(int)}：主炮伤害 1 级 +30%，之后每级 +15%；</li>
 *   <li>{@link #percentForLevel(int, LivingEntity)}：火力提升的增伤百分比（(6 + 3×(L−1)) × P 点）。</li>
 * </ul>
 */
public final class SpellMath {
   private static final ResourceLocation DANMAKU_POWER = new ResourceLocation("touhou_spell", "danmaku_spell_power");
   private static final ResourceLocation SPELL_POWER = new ResourceLocation("irons_spellbooks", "spell_power");
   /** 火力提升里"属性增幅"只生效一半（2.1.23）。 */
   public static final double ATTRIBUTE_GAIN_DAMPING = 0.5;

   /** 主炮伤害：1 级 +30%，之后每级 +15%（2.1.26）。 */
   public static final double MAIN_DAMAGE_FIRST_LEVEL = 0.30;
   public static final double MAIN_DAMAGE_PER_LEVEL = 0.15;

   private SpellMath() {
   }

   private static double attribute(LivingEntity entity, ResourceLocation id, double fallback) {
      Attribute attribute = (Attribute)ForgeRegistries.ATTRIBUTES.getValue(id);
      if (attribute == null) {
         return fallback;
      }

      double value = entity.m_21133_(attribute);
      return value > 0.0 ? value : fallback;
   }

   /**
    * 我方伤害乘区：**只含 ISS 法术强度**（缺省 1.0）。
    *
    * <p>2.1.22：弹幕学强度（{@code touhou_spell:danmaku_spell_power}）不再由我方乘进来 ——
    * {@code youkaishomecoming} 的 {@code YHAttackListener#onHurt} 已经会按
    * {@code CardPowerCompat}（硬编码读同一个属性）对弹幕伤害乘一次
    * {@code (1 + power/400) × 弹幕学强度}，我方再乘就是强度平方（曾导致单发 17.6 → 58 伤）。
    *
    * @see #danmakuStrength(LivingEntity)
    */
   public static double multiplier(LivingEntity entity) {
      return entity == null ? 1.0 : attribute(entity, SPELL_POWER, 1.0);
   }

   /** 弹幕学强度（只读，用于显示；实际结算由 YHC 的擦弹加成完成）。 */
   public static double danmakuStrength(LivingEntity entity) {
      return entity == null ? 1.0 : attribute(entity, DANMAKU_POWER, 1.0);
   }

   /**
    * 火力提升的增伤百分比（2.1.3：按 P 点数缩放）。
    *
    * <p>1 级 = {@code 6 × P}%，之后每级 +{@code 3 × P}%；P 点上限 5（满 P：30/45/60/75/90…），
    * P = 0 时没有任何效果。结果仍乘「弹幕学强度 × 法术强度」，与既有口径一致。
    */
   public static double percentForLevel(int effectiveLevel, LivingEntity entity) {
      double points = (double)PowerPoints.of(entity);
      double attributeGain = danmakuStrength(entity) * multiplier(entity);
      double damped = 1.0 + (attributeGain - 1.0) * ATTRIBUTE_GAIN_DAMPING;
      return (6.0 + 3.0 * (double)(Math.max(1, effectiveLevel) - 1)) * points * damped;
   }

   /** 子机数量：1 + 有效等级，封顶 7（{@code extra} 是后续装备预留的加值端口）。 */
   public static int optionCount(int level) {
      return optionCount(level, 0);
   }

   /** 子机数量：1 + 有效等级 + 额外加值，最少 1（2.1.26 起取消上限）。 */
   public static int optionCount(int level, int extra) {
      return Math.max(1, 1 + Math.max(0, level) + Math.max(0, extra));
   }

   /** 主炮伤害倍率：1 级 +30%，之后每级 +15%；等级 0（未开）为 1.0。 */
   public static double mainDamageMultiplier(int level) {
      int effective = Math.max(0, level);
      return effective <= 0
         ? 1.0
         : 1.0 + MAIN_DAMAGE_FIRST_LEVEL + MAIN_DAMAGE_PER_LEVEL * (double)(effective - 1);
   }

   /** 数字显示：整数不带小数，否则保留一位。 */
   public static String format(double value) {
      return Math.abs(value - Math.rint(value)) < 0.05 ? Long.toString(Math.round(value)) : String.format(Locale.ROOT, "%.1f", value);
   }
}

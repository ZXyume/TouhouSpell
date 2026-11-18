package org.zxyume.touhou_spell.extra;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.zxyume.touhou_spell.extra.spell.FlyModeSpell;

/**
 * 把"轮盘里的法术等级"换算成飞行术 / 火力提升的效果 amplifier（2.0.0 起只剩这两种）。
 *
 * <p>等级来源统一走 {@link WheelScanner}（单一扫描实现 + 10 tick 缓存），有效等级走
 * {@code AbstractSpell#getLevelFor}（含亲和戒指与 {@code ModifySpellLevelEvent}），
 * 之后再交给 {@link SpellMath#percentForLevel} 之类的"有效等级"口径换算，
 * 因此亲和加成只会被计算一次。
 */
public final class WheelLevels {
   /** amplifier 的合法范围（MobEffectInstance 用 byte 存等级）。 */
   private static final int MAX_AMPLIFIER = 255;
   private static final Logger LOGGER = LogUtils.getLogger();

   private WheelLevels() {
   }

   /** 轮盘里该法术的基础等级；不在轮盘里返回 -1。 */
   public static int baseLevel(Player player, AbstractSpell spell) {
      if (player == null || spell == null) {
         return -1;
      }

      return WheelScanner.baseLevel(player, spell.getSpellResource());
   }

   /** 轮盘里该法术的有效等级；不在轮盘里返回 0（效果只有一级时，等级从这里读）。 */
   public static int activeLevel(Player player, AbstractSpell spell) {
      if (player == null || spell == null) {
         return 0;
      }

      return baseLevel(player, spell) < 0 ? 0 : effectiveLevel(player, spell);
   }

   /** 轮盘里该法术的"有效等级"（含亲和戒指 / 事件修正）；不在轮盘里时退回法术最低等级。 */
   public static int effectiveLevel(Player player, AbstractSpell spell) {
      if (spell == null) {
         return 1;
      }

      int base = baseLevel(player, spell);
      if (base < 0) {
         base = spell.getMinLevel();
      }

      try {
         return spell.getLevelFor(base, player);
      } catch (Throwable throwable) {
         LOGGER.debug("[touhou_spell] getLevelFor failed for {}, using the base level", spell.getSpellResource(), throwable);
         return base;
      }
   }

   /** 火力提升：amplifier = floor(percentForLevel(有效等级))，与施法当刻的口径一致。 */
   public static int powerUpAmplifier(Player player) {
      int level = effectiveLevel(player, TouhouSpellExtra.POWER_UP_SPELL);
      return clamp((int)Math.floor(SpellMath.percentForLevel(level, player)));
   }

   /** 飞行术：amplifier = min(有效等级, MAX_EFFECT_LEVEL) - 1，与 {@code FlyModeSpell#onCast} 一致。 */
   public static int flyAmplifier(Player player) {
      int level = effectiveLevel(player, TouhouSpellExtra.FLY_SPELL);
      return clamp(Math.min(Math.max(1, level), FlyModeSpell.MAX_EFFECT_LEVEL) - 1);
   }

   private static int clamp(int amplifier) {
      return Math.max(0, Math.min(MAX_AMPLIFIER, amplifier));
   }
}

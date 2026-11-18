package org.zxyume.touhou_spell.util;

import com.mojang.logging.LogUtils;
import dev.xkmc.youkaishomecoming.init.registrate.YHDanmaku.Bullet;
import dev.xkmc.youkaishomecoming.init.registrate.YHDanmaku.Laser;
import java.util.EnumMap;
import java.util.Map;
import org.slf4j.Logger;

/**
 * 弹种的数值表（2.0.0）。
 *
 * <p>2.0.0 删掉了"弹幕射速"机制，耗蓝也改成"弹种自身伤害值"（长激光除外，固定
 * {@link #LASER_MANA_COST}），所以这里只剩投射物初速用的 {@link #speedMod}。
 */
public enum DanmakuCategory {
   CIRCLE(1.0F),
   BALL(1.0F),
   MENTOS(1.0F),
   BUBBLE(1.0F),
   BUTTERFLY(0.3F),
   SPARK(2.0F),
   STAR(1.5F),
   LASER(1.0F),
   PENCIL(1.0F),
   DEFAULT(1.0F);

   /** 长激光（{@link Laser#LASER}）的固定耗蓝；其余弹种耗蓝 = 自身伤害值。 */
   public static final double LASER_MANA_COST = 40.0;

   public final float speedMod;
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<Bullet, DanmakuCategory> CACHE = new EnumMap<>(Bullet.class);
   private static final Map<Laser, DanmakuCategory> LASER_CACHE = new EnumMap<>(Laser.class);

   DanmakuCategory(float speedMod) {
      this.speedMod = speedMod;
   }

   public static DanmakuCategory fromBullet(Bullet bullet) {
      return CACHE.getOrDefault(bullet, DEFAULT);
   }

   public static DanmakuCategory fromLaser(Laser laser) {
      return LASER_CACHE.getOrDefault(laser, DEFAULT);
   }

   static {
      for (Bullet bullet : Bullet.values()) {
         try {
            CACHE.put(bullet, valueOf(bullet.name()));
         } catch (IllegalArgumentException var5) {
            LOGGER.debug("[touhou_spell] no DanmakuCategory for bullet {}, using DEFAULT", bullet.name());
            CACHE.put(bullet, DEFAULT);
         }
      }

      for (Laser laser : Laser.values()) {
         try {
            LASER_CACHE.put(laser, valueOf(laser.name()));
         } catch (IllegalArgumentException var4) {
            LOGGER.debug("[touhou_spell] no DanmakuCategory for laser {}, using DEFAULT", laser.name());
            LASER_CACHE.put(laser, DEFAULT);
         }
      }
   }
}

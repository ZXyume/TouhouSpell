package org.zxyume.touhou_spell.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class DanmakuMath {
   public static Vec3 getOptionPos(Player player, int index, int total, int ticks, float pTick) {
      double x = Mth.m_14139_((double)pTick, player.f_19854_, player.m_20185_());
      double y = Mth.m_14139_((double)pTick, player.f_19855_, player.m_20186_()) + (double)player.m_20192_();
      double z = Mth.m_14139_((double)pTick, player.f_19856_, player.m_20189_());
      Vec3 eyePos = new Vec3(x, y, z);
      float yRot = Mth.m_14179_(pTick, player.f_19859_, player.m_146908_());
      Vec3 look = Vec3.m_82498_(0.0F, yRot).m_82541_();
      Vec3 back = look.m_82490_(-1.0);
      Vec3 right = look.m_82537_(new Vec3(0.0, 1.0, 0.0)).m_82541_();
      // 1.5.3：圆心沿朝向前移 2 格 + 半径 3.5 + 张角 60°
      double radius = 3.5;
      double arcDegrees = 60.0;
      double fraction = total > 1 ? (double)index / (double)(total - 1) - 0.5 : 0.0;
      double angleRad = Math.toRadians(fraction * arcDegrees);
      double hover = Math.sin((double)((float)ticks + pTick + (float)(index * 10)) * 0.1) * 0.05;
      Vec3 offset = back.m_82490_(Math.cos(angleRad)).m_82549_(right.m_82490_(Math.sin(angleRad))).m_82490_(radius);
      // 圆心沿玩家水平朝向前移 2 格（俯仰角不参与）
      Vec3 center = eyePos.m_82549_(look.m_82490_(2.0));
      return center.m_82549_(offset).m_82520_(0.0, -0.4 + hover, 0.0);
   }
}

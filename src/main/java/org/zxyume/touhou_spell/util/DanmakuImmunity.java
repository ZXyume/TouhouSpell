package org.zxyume.touhou_spell.util;

import dev.xkmc.youkaishomecoming.content.entity.danmaku.IYHDanmaku;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.entity.PartEntity;

public final class DanmakuImmunity {
   private DanmakuImmunity() {
   }

   /**
    * Player-owned danmaku ignores the target's danmaku invulnerability window.
    *
    * <p>Youkai's Homecoming implements its own repeat-hit guard inside
    * {@code IYHDanmaku#hurtTarget}: when the target is still inside a hurt window
    * ({@code LivingEntity#hurtTime} &gt; 0, SRG {@code f_20916_}) and the last damage came from
    * another {@code IYHDanmaku}, the hit is dropped. Clearing {@code hurtTime} before the hit is
    * therefore what lets a rapid-fire barrage land every bullet.</p>
    *
    * <p>{@code Entity#invulnerableTime} ({@code f_19802_}) does not need clearing here: danmaku
    * damage is registered in {@code minecraft:bypasses_cooldown} by the YHC datapack.</p>
    */
   public static void bypassInvulnerability(IYHDanmaku var0, Entity var1) {
      Entity var2 = var0.self().m_19749_();
      if (var2 instanceof Player) {
         while (var1 instanceof PartEntity) {
            var1 = ((PartEntity)var1).getParent();
         }

         if (var1 instanceof LivingEntity var3) {
            var3.f_20916_ = 0;
         }
      }
   }
}

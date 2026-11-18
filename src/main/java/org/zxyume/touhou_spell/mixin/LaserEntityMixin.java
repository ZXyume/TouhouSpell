package org.zxyume.touhou_spell.mixin;

import dev.xkmc.fastprojectileapi.collision.LaserHitHelper;
import dev.xkmc.youkaishomecoming.content.entity.danmaku.IYHDanmaku;
import dev.xkmc.youkaishomecoming.content.entity.danmaku.YHBaseLaserEntity;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zxyume.touhou_spell.util.DanmakuImmunity;

/**
 * Player owned lasers ignore the target's danmaku invulnerability window.
 *
 * <p>This hooks the head of {@code YHBaseLaserEntity#onHit(LaserHitResult)} and clears the hurt
 * window of every entity the laser is about to hit, instead of redirecting the per-entity
 * {@code hurtTarget} call. A redirect on that call site collides with
 * {@code bypasses_cooldown}'s own redirect (same target, same priority): mixin then skips one
 * of the two handlers, and with {@code require = 1} that becomes a fatal injection error which
 * aborts the game during mod construction. A HEAD {@code @Inject} has no such conflict, so
 * {@code require = 1} is safe here.</p>
 */
@Mixin(value = YHBaseLaserEntity.class, remap = false)
public abstract class LaserEntityMixin {
   @Inject(
      method = {"onHit"},
      at = {@At("HEAD")},
      remap = false,
      require = 0
   )
   private void touhou_spell$bypassInvulnerability(LaserHitHelper.LaserHitResult result, CallbackInfo callback) {
      if (result == null || result.ehit() == null) {
         return;
      }

      for (EntityHitResult hit : result.ehit()) {
         DanmakuImmunity.bypassInvulnerability((IYHDanmaku)this, hit.m_82443_());
      }
   }
}

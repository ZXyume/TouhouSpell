package org.zxyume.touhou_spell.mixin;

import dev.xkmc.youkaishomecoming.content.entity.danmaku.IYHDanmaku;
import dev.xkmc.youkaishomecoming.content.entity.danmaku.YHBaseDanmakuEntity;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.zxyume.touhou_spell.util.DanmakuImmunity;

@Mixin(
   value = {YHBaseDanmakuEntity.class},
   remap = false
)
public abstract class DanmakuEntityMixin {
   @Inject(
      method = {"onHitEntity"},
      at = {@At("HEAD")},
      remap = false,
      require = 0
   )
   private void touhou_spell$bypassInvulnerability(EntityHitResult var1, CallbackInfo var2) {
      DanmakuImmunity.bypassInvulnerability((IYHDanmaku)this, var1.m_82443_());
   }
}

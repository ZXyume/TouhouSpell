package org.zxyume.touhou_spell.capabilities;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

public class DanmakuManager {

   public static LazyOptional<DanmakuHandler> getHandler(Player player) {
      return player.getCapability(DanmakuProvider.CAPABILITY);
   }
}

package org.zxyume.touhou_spell.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import org.zxyume.touhou_spell.capabilities.DanmakuManager;

public class ClientPacketHandler {
   public static void handleDanmakuSync(CompoundTag tag) {
      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (player != null) {
         DanmakuManager.getHandler(player).ifPresent(handler -> handler.deserializeNBT(tag));
      }
   }
}

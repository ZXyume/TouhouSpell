package org.zxyume.touhou_spell.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.zxyume.touhou_spell.network.packet.DanmakuSyncPacket;
import org.zxyume.touhou_spell.network.packet.ServerboundCtrlStatePacket;

public class PacketHandler {
   public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
      ResourceLocation.fromNamespaceAndPath("touhou_spell", "main"), () -> "1.0", s -> true, s -> true
   );

   public static void init() {
      int id = 0;
      CHANNEL.messageBuilder(DanmakuSyncPacket.class, ++id, NetworkDirection.PLAY_TO_CLIENT)
         .decoder(DanmakuSyncPacket::decode)
         .encoder(DanmakuSyncPacket::encode)
         .consumerMainThread(DanmakuSyncPacket::handle)
         .add();
      CHANNEL.messageBuilder(ServerboundCtrlStatePacket.class, ++id, NetworkDirection.PLAY_TO_SERVER)
         .decoder(ServerboundCtrlStatePacket::decode)
         .encoder(ServerboundCtrlStatePacket::encode)
         .consumerMainThread(ServerboundCtrlStatePacket::handle)
         .add();
   }

   public static <MSG> void sendToServer(MSG message) {
      CHANNEL.sendToServer(message);
   }
}

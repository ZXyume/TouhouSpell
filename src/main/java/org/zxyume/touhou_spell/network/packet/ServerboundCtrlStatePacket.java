package org.zxyume.touhou_spell.network.packet;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent.Context;
import org.zxyume.touhou_spell.extra.StaffCtrlState;

/** 1.4.0：客户端把"是否按着 Ctrl"同步给服务端（只在状态变化时发送）。 */
public record ServerboundCtrlStatePacket(boolean ctrlDown) {
   public void handle(Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(
         () -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
               StaffCtrlState.set(player, this.ctrlDown);
            }
         }
      );
      context.setPacketHandled(true);
   }

   public static void encode(ServerboundCtrlStatePacket msg, FriendlyByteBuf buffer) {
      buffer.writeBoolean(msg.ctrlDown);
   }

   public static ServerboundCtrlStatePacket decode(FriendlyByteBuf buffer) {
      return new ServerboundCtrlStatePacket(buffer.readBoolean());
   }
}

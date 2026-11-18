package org.zxyume.touhou_spell.network.packet;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;
import org.zxyume.touhou_spell.client.ClientPacketHandler;

public record DanmakuSyncPacket(CompoundTag tag) {
   public static void encode(DanmakuSyncPacket msg, FriendlyByteBuf buffer) {
      buffer.m_130079_(msg.tag);
   }

   public static DanmakuSyncPacket decode(FriendlyByteBuf buffer) {
      return new DanmakuSyncPacket(buffer.m_130260_());
   }

   public void handle(Supplier<Context> contextSupplier) {
      Context context = contextSupplier.get();
      context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleDanmakuSync(this.tag)));
      context.setPacketHandled(true);
   }
}

package org.zxyume.touhou_spell.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.zxyume.touhou_spell.network.PacketHandler;
import org.zxyume.touhou_spell.network.packet.ServerboundCtrlStatePacket;

/**
 * 1.4.0：把 Ctrl（疾跑键）的按下状态同步给服务端，用于"Ctrl + 右键打开更换弹幕面板"。
 *
 * <p>只在状态变化时发一条包（不是每 tick 都发）。
 */
@EventBusSubscriber(
   modid = "touhou_spell",
   value = {Dist.CLIENT},
   bus = Bus.FORGE
)
public final class StaffCtrlTracker {
   private static boolean lastSent;

   private StaffCtrlTracker() {
   }

   @SubscribeEvent
   public static void onClientTick(TickEvent.ClientTickEvent event) {
      if (event.phase != TickEvent.Phase.END) {
         return;
      }

      Minecraft minecraft = Minecraft.m_91087_();
      if (minecraft.f_91074_ == null) {
         return;
      }

      boolean down = minecraft.f_91066_.f_92091_.m_90857_();
      if (down != lastSent) {
         lastSent = down;
         PacketHandler.sendToServer(new ServerboundCtrlStatePacket(down));
      }
   }
}

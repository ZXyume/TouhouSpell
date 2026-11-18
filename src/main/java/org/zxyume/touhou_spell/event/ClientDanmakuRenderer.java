package org.zxyume.touhou_spell.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.zxyume.touhou_spell.extra.item.KaleidoCoinStaffItem;
import org.zxyume.touhou_spell.render.OptionCircleRenderer;

/**
 * 1.2.0：开火状态不再是能力，改为"手持万花币即常显 option 圆环"。
 */
@EventBusSubscriber(
   modid = "touhou_spell",
   value = {Dist.CLIENT},
   bus = Bus.FORGE
)
public class ClientDanmakuRenderer {
   @SubscribeEvent
   public static void onRenderLevelStage(RenderLevelStageEvent event) {
      if (event.getStage() == Stage.AFTER_ENTITIES) {
         LocalPlayer player = Minecraft.m_91087_().f_91074_;
         if (player != null) {
            for (Player targetPlayer : player.m_9236_().m_6907_()) {
               if (holdingStaff(targetPlayer)) {
                  int light = LevelRenderer.m_109541_(targetPlayer.m_9236_(), targetPlayer.m_20183_());
                  OptionCircleRenderer.renderOptions(
                     event.getPoseStack(), Minecraft.m_91087_().m_91269_().m_110104_(), light, targetPlayer, event.getPartialTick()
                  );
               }
            }
         }
      }
   }

   private static boolean holdingStaff(Player player) {
      return player.m_21205_().m_41720_() instanceof KaleidoCoinStaffItem
         || player.m_21206_().m_41720_() instanceof KaleidoCoinStaffItem;
   }
}

package org.zxyume.touhou_spell.extra.client;

import io.redspace.ironsspellbooks.render.SpellBookCurioRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(
   modid = "touhou_spell",
   value = {Dist.CLIENT},
   bus = Bus.MOD
)
public final class ClientSetupEvents {
   private ClientSetupEvents() {
   }

   @SubscribeEvent
   public static void onClientSetup(FMLClientSetupEvent var0) {
      if (TouhouSpellExtra.DANMAKU_GRIMOIRE != null) {
         CuriosRendererRegistry.register(TouhouSpellExtra.DANMAKU_GRIMOIRE, SpellBookCurioRenderer::new);
      }
   }
}

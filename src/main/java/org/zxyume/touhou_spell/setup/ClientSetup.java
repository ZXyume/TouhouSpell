package org.zxyume.touhou_spell.setup;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.zxyume.touhou_spell.client.KaleidoCoinItemDeco;
import org.zxyume.touhou_spell.init.ModItem;
import org.zxyume.touhou_spell.init.ModMenus;
import org.zxyume.touhou_spell.menu.DanmakuScreen;

@EventBusSubscriber(
   modid = "touhou_spell",
   bus = Bus.MOD,
   value = {Dist.CLIENT}
)
public class ClientSetup {
   @SubscribeEvent
   public static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(() -> MenuScreens.m_96206_((MenuType)ModMenus.DANMAKU_MENU.get(), DanmakuScreen::new));
   }

   @SubscribeEvent
   public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
      event.register(ModItem.KALEIDO_COIN.get(), new KaleidoCoinItemDeco());
   }
}

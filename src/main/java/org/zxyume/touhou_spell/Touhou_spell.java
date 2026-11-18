package org.zxyume.touhou_spell;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.zxyume.touhou_spell.init.ModAttribute;
import org.zxyume.touhou_spell.init.ModCreativeTab;
import org.zxyume.touhou_spell.init.ModEffect;
import org.zxyume.touhou_spell.init.ModItem;
import org.zxyume.touhou_spell.init.ModMenus;
import org.zxyume.touhou_spell.init.ModSchool;
import org.zxyume.touhou_spell.init.ModSpell;
import org.zxyume.touhou_spell.network.PacketHandler;

@Mod("touhou_spell")
public class Touhou_spell {
   public static final String MODID = "touhou_spell";
   private static final Logger LOGGER = LogUtils.getLogger();

   public Touhou_spell() {
      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
      ModSpell.register(modEventBus);
      ModAttribute.register(modEventBus);
      ModMenus.register(modEventBus);
      ModSchool.register(modEventBus);
      ModItem.register(modEventBus);
      ModCreativeTab.register(modEventBus);
      ModEffect.register(modEventBus);
      modEventBus.addListener(this::commonSetup);
      MinecraftForge.EVENT_BUS.register(this);
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      event.enqueueWork(() -> PacketHandler.init());
   }

   public static ResourceLocation id(@NotNull String path) {
      return ResourceLocation.fromNamespaceAndPath("touhou_spell", path);
   }

   public static ResourceLocation loc(String id) {
      return ResourceLocation.fromNamespaceAndPath("touhou_spell", id);
   }
}

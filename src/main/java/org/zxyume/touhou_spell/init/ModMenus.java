package org.zxyume.touhou_spell.init;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.zxyume.touhou_spell.menu.DanmakuMenu;

public class ModMenus {
   public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "touhou_spell");
   public static final RegistryObject<MenuType<DanmakuMenu>> DANMAKU_MENU = MENUS.register("ship_ammo_menu", () -> IForgeMenuType.create(DanmakuMenu::new));

   public static void register(IEventBus eventBus) {
      MENUS.register(eventBus);
   }
}

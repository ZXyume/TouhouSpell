package org.zxyume.touhou_spell.init;

import dev.xkmc.youkaishomecoming.init.registrate.YHDanmaku;
import dev.xkmc.youkaishomecoming.init.registrate.YHItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTab {
   private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.f_279569_, "touhou_spell");
   public static final RegistryObject<CreativeModeTab> EQUIPMENT_TAB = TABS.register(
      "spellbook_equipment",
      () -> CreativeModeTab.builder()
            .m_257941_(Component.m_237115_("itemGroup.touhou_spell.equipment_tab"))
            .m_257737_(() -> new ItemStack((ItemLike)YHItems.CIRNO_WINGS.get()))
            .m_257501_((enabledFeatures, entries) -> {
               entries.m_246326_((ItemLike)ModItem.MAID_CHESTPLATE.get());
               entries.m_246326_((ItemLike)ModItem.MAID_LEGGINGS.get());
               entries.m_246326_((ItemLike)ModItem.MAID_BOOTS.get());
               entries.m_246326_((ItemLike)ModItem.KALEIDO_COIN.get());
            })
            .withTabsBefore(new ResourceKey[]{YHDanmaku.TAB.getKey()})
            .m_257652_()
   );

   public static void register(IEventBus eventBus) {
      TABS.register(eventBus);
   }
}

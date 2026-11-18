package org.zxyume.touhou_spell.init;

import io.redspace.ironsspellbooks.api.attribute.MagicRangedAttribute;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(
   modid = "touhou_spell",
   bus = Bus.MOD
)
public class ModAttribute {
   private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, "touhou_spell");
   public static final RegistryObject<Attribute> D_SPELL_POWER = ATTRIBUTES.register(
      "danmaku_spell_power", () -> new MagicRangedAttribute("attribute.touhou_spell.danmaku_spell_power", 1.0, 0.0, 1000000.0).m_22084_(true)
   );
   public static final RegistryObject<Attribute> D_POWER = ATTRIBUTES.register(
      "danmaku_power", () -> new MagicRangedAttribute("attribute.touhou_spell.danmaku_power", 0.0, 0.0, 1000000.0).m_22084_(true)
   );
   public static final RegistryObject<Attribute> D_OPTION = ATTRIBUTES.register(
      "danmaku_option_count", () -> new MagicRangedAttribute("attribute.touhou_spell.option_count", 1.0, 0.0, 1000000.0).m_22084_(true)
   );

   @SubscribeEvent
   public static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
      // 1.1.2：原先遍历所有实体类型（启动期开销大且毫无意义）；这些属性只被玩家使用。
      for (RegistryObject<Attribute> attribute : ATTRIBUTES.getEntries()) {
         event.add(EntityType.f_20532_, attribute.get());
      }
   }

   public static void register(IEventBus var0) {
      ATTRIBUTES.register(var0);
   }
}

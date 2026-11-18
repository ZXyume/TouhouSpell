package org.zxyume.touhou_spell.entity.armor.maid;

import net.minecraft.resources.ResourceLocation;
import org.zxyume.touhou_spell.item.armor.MaidArmorItem;
import software.bernie.geckolib.model.GeoModel;

public class MaidArmorModel extends GeoModel<MaidArmorItem> {
   public ResourceLocation getModelResource(MaidArmorItem object) {
      return ResourceLocation.fromNamespaceAndPath("touhou_spell", "geo/maid_suit.geo.json");
   }

   public ResourceLocation getTextureResource(MaidArmorItem object) {
      return ResourceLocation.fromNamespaceAndPath("touhou_spell", "textures/models/armor/maid_suit.png");
   }

   public ResourceLocation getAnimationResource(MaidArmorItem animatable) {
      return ResourceLocation.fromNamespaceAndPath("irons_spellbooks", "animations/wizard_armor_animation.json");
   }
}

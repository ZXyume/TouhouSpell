package org.zxyume.touhou_spell.entity.armor.maid;

import io.redspace.ironsspellbooks.entity.armor.GenericCustomArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import org.zxyume.touhou_spell.item.armor.MaidArmorItem;
import software.bernie.geckolib.model.GeoModel;

public class MaidArmorRenderer extends GenericCustomArmorRenderer<MaidArmorItem> {
   public MaidArmorRenderer(GeoModel<MaidArmorItem> model) {
      super(model);
   }

   protected void applyBoneVisibilityBySlot(EquipmentSlot currentSlot) {
      super.applyBoneVisibilityBySlot(currentSlot);
      if (null != null) {
         this.setBoneVisible(null, currentSlot == EquipmentSlot.LEGS);
      }
   }

   public void applyBoneVisibilityByPart(EquipmentSlot currentSlot, ModelPart currentPart, HumanoidModel<?> model) {
      super.applyBoneVisibilityByPart(currentSlot, currentPart, model);
      if (null != null) {
         boolean shouldShow = currentPart == model.f_102810_ && currentSlot == EquipmentSlot.LEGS;
         this.setBoneVisible(null, shouldShow);
      }
   }
}

package org.zxyume.touhou_spell.extra.item;

import io.redspace.ironsspellbooks.item.UpgradeOrbItem;
import io.redspace.ironsspellbooks.item.armor.UpgradeOrbType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ExtraUpgradeOrbItem extends UpgradeOrbItem {
   public ExtraUpgradeOrbItem(Properties var1, ResourceKey<UpgradeOrbType> var2) {
      super(var1, var2);
   }

   public String getCreatorModId(ItemStack var1) {
      return "touhou_spell";
   }
}

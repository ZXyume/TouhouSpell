package org.zxyume.touhou_spell.item.armor;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zxyume.touhou_spell.entity.armor.maid.MaidArmorModel;
import org.zxyume.touhou_spell.entity.armor.maid.MaidArmorRenderer;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class MaidArmorItem extends ImbuableChestplateArmorItem implements UniqueItem {
   private static final ResourceLocation RELEASE_CARD = new ResourceLocation("touhou_spell", "cast_spell_card");
   private static final int SPELL_LEVEL = 1;
   private static final String SEED_FLAG = "touhou_spell:maid_card_spell";

   public MaidArmorItem(Type var1, Properties var2) {
      super(ArmorMaterials.MAID, var1, var2, new AttributeContainer[0]);
   }

   public void initializeSpellContainer(ItemStack var1) {
      super.initializeSpellContainer(var1);
      if (var1 != null && this.m_266204_() == Type.CHESTPLATE) {
         if (ISpellContainer.isSpellContainer(var1)) {
            ISpellContainer var3 = ISpellContainer.get(var1);
            SpellData var4 = var3.getSpellAtIndex(0);
            AbstractSpell var5 = var4 == null ? null : var4.getSpell();
            if (var5 != null && var5 != SpellRegistry.none()) {
               if (RELEASE_CARD.toString().equals(var5.getSpellId()) && !var4.isLocked()) {
                  ISpellContainerMutable var9 = var3.mutableCopy();
                  var9.removeSpellAtIndex(0);
                  var9.addSpellAtIndex(var5, var4.getLevel(), 0, true);
                  ISpellContainer.set(var1, var9.toImmutable());
               }

               // 2.1.6：set() 可能重建 tag，所以在它之后再取一次
               var1.m_41784_().m_128379_(SEED_FLAG, true);
            } else {
               AbstractSpell var6;
               try {
                  var6 = SpellRegistry.getSpell(RELEASE_CARD);
               } catch (Throwable var8) {
                  return;
               }

               if (var6 != null && RELEASE_CARD.toString().equals(var6.getSpellId())) {
                  ISpellContainerMutable var7 = var3.mutableCopy();
                  if (!var3.isEmpty()) {
                     var7.removeSpellAtIndex(0);
                  }

                  var7.addSpell(var6, SPELL_LEVEL, true);
                  ISpellContainer.set(var1, var7.toImmutable());
                  var1.m_41784_().m_128379_(SEED_FLAG, true);
               }
            }
         }
      }
   }

   public boolean isDamageable(ItemStack var1) {
      return this.m_41465_();
   }

   @OnlyIn(Dist.CLIENT)
   public GeoArmorRenderer<?> supplyRenderer() {
      return new MaidArmorRenderer(new MaidArmorModel());
   }

   public boolean m_8120_(ItemStack var1) {
      return this.getMaxStackSize(var1) == 1;
   }
}

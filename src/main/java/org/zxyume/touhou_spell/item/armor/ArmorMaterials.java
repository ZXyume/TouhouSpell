package org.zxyume.touhou_spell.item.armor;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.armor.IronsExtendedArmorMaterial;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.zxyume.touhou_spell.init.ModAttribute;

public enum ArmorMaterials implements IronsExtendedArmorMaterial {
   MAID(
      "maid",
      325,
      makeArmorMap(3, 7, 6, 3),
      35,
      SoundEvents.f_11678_,
      1.0F,
      0.0F,
      () -> Ingredient.m_43929_(new ItemLike[]{Items.f_42616_}),
      Map.of(
         (Attribute)AttributeRegistry.MAX_MANA.get(),
         new AttributeModifier("Max Mana", 99.0, Operation.ADDITION),
         (Attribute)ModAttribute.D_SPELL_POWER.get(),
         new AttributeModifier("Danmaku Spell", 0.15, Operation.MULTIPLY_BASE)
      )
   );

   private static final int[] HEALTH_PER_SLOT = new int[]{13, 15, 16, 11};
   private static final EnumMap<Type, Integer> HEALTH_FUNCTION_FOR_TYPE = (EnumMap<Type, Integer>)Util.m_137469_(new EnumMap(Type.class), var0 -> {
      var0.put(Type.BOOTS, 13);
      var0.put(Type.LEGGINGS, 15);
      var0.put(Type.CHESTPLATE, 16);
      var0.put(Type.HELMET, 11);
   });
   private final String name;
   private final int durabilityMultiplier;
   private final EnumMap<Type, Integer> protectionFunctionForType;
   private final int enchantmentValue;
   private final SoundEvent sound;
   private final float toughness;
   private final float knockbackResistance;
   private final LazyLoadedValue<Ingredient> repairIngredient;
   private final Map<Attribute, AttributeModifier> additionalAttributes;

   private ArmorMaterials(
      String var3,
      int var4,
      EnumMap<Type, Integer> var5,
      int var6,
      SoundEvent var7,
      float var8,
      float var9,
      Supplier<Ingredient> var10,
      Map<Attribute, AttributeModifier> var11
   ) {
      this.name = var3;
      this.durabilityMultiplier = var4;
      this.protectionFunctionForType = var5;
      this.enchantmentValue = var6;
      this.sound = var7;
      this.toughness = var8;
      this.knockbackResistance = var9;
      this.repairIngredient = new LazyLoadedValue(var10);
      this.additionalAttributes = var11;
   }

   public static EnumMap<Type, Integer> makeArmorMap(int var0, int var1, int var2, int var3) {
      return (EnumMap<Type, Integer>)Util.m_137469_(new EnumMap(Type.class), var4 -> {
         var4.put(Type.BOOTS, var3);
         var4.put(Type.LEGGINGS, var2);
         var4.put(Type.CHESTPLATE, var1);
         var4.put(Type.HELMET, var0);
      });
   }

   public static EnumMap<Type, Integer> schoolArmorMap() {
      return makeArmorMap(3, 8, 6, 3);
   }

   public int getDurabilityForSlot(EquipmentSlot var1) {
      return HEALTH_PER_SLOT[var1.m_20749_()] * this.durabilityMultiplier;
   }

   public int m_266425_(Type var1) {
      return HEALTH_FUNCTION_FOR_TYPE.get(var1) * this.durabilityMultiplier;
   }

   public int m_7366_(Type var1) {
      return this.protectionFunctionForType.get(var1);
   }

   public int m_6646_() {
      return this.enchantmentValue;
   }

   public SoundEvent m_7344_() {
      return this.sound;
   }

   public Ingredient m_6230_() {
      return (Ingredient)this.repairIngredient.m_13971_();
   }

   public String m_6082_() {
      return this.name;
   }

   public float m_6651_() {
      return this.toughness;
   }

   public Map<Attribute, AttributeModifier> getAdditionalAttributes() {
      return this.additionalAttributes;
   }

   public float m_6649_() {
      return this.knockbackResistance;
   }
}

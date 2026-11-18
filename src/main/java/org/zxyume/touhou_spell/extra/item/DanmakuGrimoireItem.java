package org.zxyume.touhou_spell.extra.item;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainerMutable;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.item.SpellBook;
import io.redspace.ironsspellbooks.item.UniqueItem;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

public class DanmakuGrimoireItem extends SpellBook implements UniqueItem {
   private static final Logger LOGGER = LogUtils.getLogger();
   /**
    * 2.0.0：火力全开与多重弹幕合并，固有法术从 4 条减到 3 条，槽位同步 3 —— 手册"开局就没有空槽位"，
    * 且不再提供任何扩容途径。
    */
   public static final int SPELL_SLOTS = 3;
   private static final ResourceLocation DANMAKU_POWER = new ResourceLocation("touhou_spell", "danmaku_spell_power");
   private static final List<DanmakuGrimoireItem.Innate> INNATE = List.of(
      new DanmakuGrimoireItem.Innate(new ResourceLocation("touhou_spell", "power_up"), 3),
      new DanmakuGrimoireItem.Innate(new ResourceLocation("touhou_spell", "full_firepower"), 1),
      new DanmakuGrimoireItem.Innate(new ResourceLocation("touhou_spell", "flymode"), 2)
   );

   public DanmakuGrimoireItem(Properties var1) {
      super(SPELL_SLOTS, var1);
      // 1.1.2：danmaku_power 属性取不到时跳过这一条，不能把 null 交给 ISS（物品注册阶段会 NPE）。
      ArrayList<AttributeContainer> attributes = new ArrayList<>(3);
      attributes.add(new AttributeContainer(() -> (Attribute)AttributeRegistry.MAX_MANA.get(), 3.0, Operation.ADDITION));
      attributes.add(new AttributeContainer(() -> (Attribute)AttributeRegistry.MANA_REGEN.get(), 0.75, Operation.MULTIPLY_BASE));
      Attribute danmakuPower = danmakuPower();
      if (danmakuPower != null) {
         attributes.add(new AttributeContainer(() -> danmakuPower, 0.15, Operation.MULTIPLY_BASE));
      } else {
         LOGGER.warn("[touhou_spell] danmaku_spell_power is missing: the grimoire will not grant the danmaku power bonus");
      }

      this.withSpellbookAttributes(attributes.toArray(new AttributeContainer[0]));
   }

   private static Attribute danmakuPower() {
      return (Attribute)ForgeRegistries.ATTRIBUTES.getValue(DANMAKU_POWER);
   }

   public void initializeSpellContainer(ItemStack var1) {
      if (var1 == null) {
         return;
      }

      if (ISpellContainer.isSpellContainer(var1) && matchesInnate(var1)) {
         return;
      }

      rebuild(var1);
   }

   /**
    * 按 {@link #INNATE} 重建法术容器；任何一个固有法术在注册表里找不到就放弃（保持物品原样）。
    *
    * @return 真的写入了容器返回 true
    */
   private static boolean rebuild(ItemStack stack) {
      ArrayList<AbstractSpell> spells = new ArrayList(INNATE.size());

      for (DanmakuGrimoireItem.Innate innate : INNATE) {
         AbstractSpell spell;
         try {
            spell = SpellRegistry.getSpell(innate.id());
         } catch (Throwable var6) {
            return false;
         }

         if (spell == null || !innate.id().toString().equals(spell.getSpellId())) {
            return false;
         }

         spells.add(spell);
      }

      ISpellContainerMutable container = ISpellContainer.create(SPELL_SLOTS, true, true).mutableCopy();

      for (int i = 0; i < INNATE.size(); i++) {
         container.addSpell(spells.get(i), INNATE.get(i).level(), true);
      }

      ISpellContainer.set(stack, container.toImmutable());
      return true;
   }

   private static boolean matchesInnate(ItemStack var0) {
      ISpellContainer var1 = ISpellContainer.get(var0);
      if (var1 != null && var1.getMaxSpellCount() == SPELL_SLOTS) {
         for (int var2 = 0; var2 < INNATE.size(); var2++) {
            SpellData var3 = var1.getSpellAtIndex(var2);
            if (var3 == null) {
               return false;
            }

            AbstractSpell var4 = var3.getSpell();
            if (var4 == null || !INNATE.get(var2).id().toString().equals(var4.getSpellId()) || var3.getLevel() != INNATE.get(var2).level()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private record Innate(ResourceLocation id, int level) {
   }
}

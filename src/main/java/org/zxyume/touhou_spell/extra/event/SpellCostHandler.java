package org.zxyume.touhou_spell.extra.event;

import io.redspace.ironsspellbooks.api.events.SpellOnCastEvent;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;

@EventBusSubscriber(
   modid = "touhou_spell",
   bus = Bus.FORGE
)
public final class SpellCostHandler {
   /**
    * Single source of truth for the spell card mana cost: used both by
    * {@code CastSpellCardSpell#baseManaCost} (the "can I cast it" check) and by the manual
    * deduction in its {@code onCast} (the vanilla cost is zeroed out below so the mana packet
    * can be synced after the card was actually released).
    */
   public static final int SPELL_CARD_MANA_COST = 150;

   private SpellCostHandler() {
   }

   @SubscribeEvent
   public static void onSpellOnCast(SpellOnCastEvent var0) {
      AbstractSpell var1 = TouhouSpellExtra.CAST_SPELL_CARD;
      if (var1 != null && var1.getSpellId().equals(var0.getSpellId())) {
         var0.setManaCost(0);
      }
   }
}

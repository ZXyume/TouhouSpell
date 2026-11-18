package org.zxyume.touhou_spell.extra.event;

import dev.xkmc.youkaishomecoming.content.item.danmaku.ISpellItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.zxyume.touhou_spell.extra.util.CardUtil;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.CurioEquipEvent;

@EventBusSubscriber(
   modid = "touhou_spell",
   bus = Bus.FORGE
)
public final class SpellCardEvents {
   private SpellCardEvents() {
   }

   /**
    * Spell cards belong to the "spell_card" curio slot only. Curios' generic "curio" slot has no
    * validators, so it happily accepts any item - including spell cards. Curios reads the result of
    * {@link CurioEquipEvent} in {@code DynamicStackHandler#isItemValid}, so denying the event keeps
    * cards out of every other slot (GUI click, shift-click auto-equip and hopper inserts all go
    * through the same check) while {@code spell_card} itself keeps working through its
    * {@code curios:spell_card} item tag.
    */
   @SubscribeEvent
   public static void onCurioEquip(CurioEquipEvent var0) {
      ItemStack var1 = var0.getStack();
      if (var1.m_41619_() || !(var1.m_41720_() instanceof ISpellItem)) {
         return;
      }

      SlotContext var2 = var0.getSlotContext();
      if (var2 != null && !CardUtil.SLOT.equals(var2.identifier())) {
         var0.setResult(Event.Result.DENY);
      }
   }
}

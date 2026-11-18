package org.zxyume.touhou_spell.extra.event;

import io.redspace.ironsspellbooks.api.events.SpellPreCastEvent;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;

@EventBusSubscriber(
   modid = "touhou_spell",
   bus = Bus.FORGE
)
public final class ScrollCastGuard {
   private ScrollCastGuard() {
   }

   @SubscribeEvent
   public static void onPreCast(SpellPreCastEvent var0) {
      if (var0.getCastSource() == CastSource.SCROLL) {
         if (var0.getSchoolType() != null && TouhouSpellExtra.DANMAKU_SCHOOL.equals(var0.getSchoolType().getId())) {
            var0.setCanceled(true);
            if (var0.getEntity() instanceof ServerPlayer var2) {
               AbstractSpell var3 = SpellRegistry.getSpell(var0.getSpellId());
               MutableComponent var4 = var3 != null ? var3.getDisplayName(var2) : Component.m_237113_(var0.getSpellId());
               var2.m_5661_(Component.m_237110_("ui.irons_spellbooks.cast_error_scroll", new Object[]{var4}).m_130940_(ChatFormatting.RED), true);
            }
         }
      }
   }
}

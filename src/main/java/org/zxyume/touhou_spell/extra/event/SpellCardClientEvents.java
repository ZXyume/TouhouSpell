package org.zxyume.touhou_spell.extra.event;

import dev.xkmc.l2library.util.raytrace.EntityTarget;
import dev.xkmc.l2library.util.raytrace.RayTraceUtil;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.zxyume.touhou_spell.extra.util.CardUtil;

@EventBusSubscriber(
   modid = "touhou_spell",
   value = {Dist.CLIENT},
   bus = Bus.FORGE
)
public final class SpellCardClientEvents {
   private static Item lastItem = null;
   private static int lastCount = -1;
   private static final EntityTarget CARD_TARGET = new EntityTarget(3.0, Math.toRadians(5.0), 10) {
      public void tickRender() {
      }
   };

   private SpellCardClientEvents() {
   }

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent var0) {
      if (var0.phase == Phase.END) {
         LocalPlayer var1 = Minecraft.m_91087_().f_91074_;
         if (var1 == null) {
            lastItem = null;
            lastCount = -1;
            if (CARD_TARGET.target != null) {
               CARD_TARGET.updateTarget(null);
            }
         } else {
            ItemStack var2 = CardUtil.getCard(var1);
            Item var3 = var2.m_41619_() ? null : var2.m_41720_();
            int var4 = var2.m_41619_() ? 0 : var2.m_41613_();
            if (var3 != lastItem || var4 != lastCount) {
               lastItem = var3;
               lastCount = var4;
               ClientMagicData.updateSpellSelectionManager();
            }

            updateOutlineTarget(var1);
         }
      }
   }

   private static void updateOutlineTarget(LocalPlayer var0) {
      if (isCastSpellCardSelected() && CardUtil.hasCard(var0)) {
         Entity var1 = CARD_TARGET.target;
         if (var1 != null && (var1.m_213877_() || var1.m_9236_() != var0.m_9236_())) {
            clearTarget();
         }

         EntityHitResult var2 = RayTraceUtil.rayTraceEntity(var0, 64.0, var1x -> var1x instanceof LivingEntity && var1x != var0 && !var1x.m_5833_());
         CARD_TARGET.updateTarget(var2 == null ? null : var2.m_82443_());
      } else {
         clearTarget();
      }
   }

   private static void clearTarget() {
      if (CARD_TARGET.target != null) {
         CARD_TARGET.updateTarget(null);
      }
   }

   private static boolean isCastSpellCardSelected() {
      SpellSelectionManager var0 = ClientMagicData.getSpellSelectionManager();
      if (var0 == null) {
         return false;
      } else {
         SpellData var1 = var0.getSelectedSpellData();
         if (var1 == null) {
            return false;
         } else {
            AbstractSpell var2 = var1.getSpell();
            return var2 != null && "touhou_spell:cast_spell_card".equals(var2.getSpellId());
         }
      }
   }
}

package org.zxyume.touhou_spell.extra.event;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.zxyume.touhou_spell.extra.WheelLevels;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;

@EventBusSubscriber(
   modid = "touhou_spell",
   bus = Bus.FORGE
)
public final class FlightHandler {
   private static final Set<UUID> GRANTED = ConcurrentHashMap.newKeySet();

   private FlightHandler() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent var0) {
      if (var0.phase == Phase.END) {
         Player var1 = var0.player;
         if (!var1.m_9236_().m_5776_()) {
            UUID var2 = var1.m_20148_();
            boolean var3 = var1.f_19797_ % 20 == 0;
            boolean var4 = var1.m_21023_(TouhouSpellExtra.FLY_EFFECT);
            if (var4) {
               if (var3) {
                  MagicData var7 = MagicData.getPlayerMagicData(var1);
                  int flyLevel = WheelLevels.flyAmplifier(var1);
                  if (var7 != null && var7.getMana() >= 30.0F) {
                     var7.addMana((float)(-Math.max(0, 3 - flyLevel)) * 5.0F);
                     var1.m_7292_(new MobEffectInstance(TouhouSpellExtra.FLY_EFFECT, 60, 0, false, false, true));
                  } else {
                     var1.m_21195_(TouhouSpellExtra.FLY_EFFECT);
                     var4 = false;
                  }
               }

               if (var4) {
                  GRANTED.add(var2);
                  if (!var1.m_150110_().f_35936_) {
                     var1.m_150110_().f_35936_ = true;
                     var1.m_6885_();
                  }
               }
            } else if (GRANTED.remove(var2) && !var1.m_7500_() && !var1.m_5830_()) {
               var1.m_150110_().f_35936_ = false;
               var1.m_150110_().f_35935_ = false;
               var1.m_6885_();
            }

            // 1.4.1：效果只保留一级（amplifier 恒 0），这里只续时长；等级在发射时从轮盘现读
            if (var3) {
               refreshAtZero(var1, TouhouSpellExtra.POWER_UP_EFFECT);
               refreshAtZero(var1, TouhouSpellExtra.MULTI_EFFECT);
            }

         }
      }
   }

   /** 效果只保留一级：只续时长，不改 amplifier。 */
   private static void refreshAtZero(Player player, MobEffect effect) {
      if (effect != null && player.m_21023_(effect)) {
         player.m_7292_(new MobEffectInstance(effect, 60, 0, false, false, true));
      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerLoggedOutEvent var0) {
      GRANTED.remove(var0.getEntity().m_20148_());
   }
}

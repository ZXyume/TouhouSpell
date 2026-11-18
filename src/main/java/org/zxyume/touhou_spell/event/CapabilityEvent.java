package org.zxyume.touhou_spell.event;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.Clone;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.zxyume.touhou_spell.capabilities.DanmakuHandler;
import org.zxyume.touhou_spell.capabilities.DanmakuManager;
import org.zxyume.touhou_spell.capabilities.DanmakuProvider;
import org.zxyume.touhou_spell.extra.StaffCtrlState;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;
import org.zxyume.touhou_spell.extra.WheelScanner;
import org.zxyume.touhou_spell.extra.event.GrimoireRestrictionHandler;
import org.zxyume.touhou_spell.network.PacketHandler;
import org.zxyume.touhou_spell.network.packet.DanmakuSyncPacket;

/**
 * 弹种能力挂载 + 齐射队列推进（1.2.0）。
 *
 * <p>1.2.0 删掉了"开火中"能力（{@code firing_state}）与无限 recast：开火状态由"是否手持万花币"
 * 决定（纯客户端判定），服务端只在有排队射击时推进队列。
 */
@EventBusSubscriber(
   modid = "touhou_spell"
)
public class CapabilityEvent {
   private static final Logger LOGGER = LogUtils.getLogger();
   /** 队列清理日志的行数上限。 */
   private static final AtomicInteger RESET_LOGGED = new AtomicInteger();
   private static final int RESET_LOG_LIMIT = 40;

   @SubscribeEvent
   public static void registerCaps(RegisterCapabilitiesEvent event) {
      event.register(DanmakuHandler.class);
   }

   @SubscribeEvent
   public static void onAttachCaps(AttachCapabilitiesEvent<Entity> event) {
      if (event.getObject() instanceof Player) {
         event.addCapability(ResourceLocation.fromNamespaceAndPath("touhou_spell", "danmaku_ammo"), new DanmakuProvider());
      }
   }

   @SubscribeEvent
   public static void onPlayerClone(Clone event) {
      event.getOriginal().reviveCaps();
      event.getOriginal()
         .getCapability(DanmakuProvider.CAPABILITY)
         .ifPresent(oldData -> event.getEntity().getCapability(DanmakuProvider.CAPABILITY).ifPresent(newData -> newData.deserializeNBT(oldData.serializeNBT())));
      event.getOriginal().invalidateCaps();
   }

   /** 推进每个玩家尚未打完的齐射（主炮连发 / 主炮为激光时的额外子机齐射）。 */
   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (!event.side.isServer() || event.phase != Phase.END) {
         return;
      }

      if (!(event.player instanceof ServerPlayer serverPlayer)) {
         return;
      }

      if (!DanmakuHandler.hasPending(serverPlayer.m_20148_())) {
         return;
      }

      MagicData magicData = MagicData.getPlayerMagicData(serverPlayer);
      if (magicData == null) {
         return;
      }

      DanmakuManager.getHandler(serverPlayer).ifPresent(handler -> handler.tickQueue(serverPlayer, magicData));
   }

   public static void syncToClient(Player player) {
      if (player instanceof ServerPlayer serverPlayer) {
         DanmakuManager.getHandler(serverPlayer)
            .ifPresent(handler -> PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new DanmakuSyncPacket(handler.serializeNBT())));
      }
   }

   @SubscribeEvent
   public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {
      syncToClient(event.getEntity());
   }

   @SubscribeEvent
   public static void onPlayerChangedDimension(PlayerChangedDimensionEvent event) {
      Player player = event.getEntity();
      resetVolley(player, "dimension change");
      if (player instanceof ServerPlayer serverPlayer) {
         resyncDanmakuEffects(serverPlayer);
      }

      syncToClient(player);
   }

   @SubscribeEvent
   public static void onPlayerRespawn(PlayerRespawnEvent event) {
      Player player = event.getEntity();
      resetVolley(player, "respawn");

      syncToClient(player);
   }

   /**
    * 跨维度（含返回传送门）后客户端玩家实体会被重建，效果列表可能丢失；把服务器上仍然存在的
    * 四个弹幕学效果重新下发一次，HUD 与 option 圆环就不会再出现"效果没了、图标还在"的错位。
    */
   private static void resyncDanmakuEffects(ServerPlayer player) {
      MobEffect[] effects = new MobEffect[]{
         TouhouSpellExtra.POWER_UP_EFFECT,
         TouhouSpellExtra.FLY_EFFECT,
         TouhouSpellExtra.MULTI_EFFECT
      };
      for (MobEffect effect : effects) {
         if (effect == null) {
            continue;
         }

         MobEffectInstance instance = player.m_21124_(effect);
         if (instance == null) {
            continue;
         }

         PacketDistributor.PLAYER.with(() -> player).send(new ClientboundUpdateMobEffectPacket(player.m_19879_(), instance));
      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerLoggedOutEvent event) {
      resetVolley(event.getEntity(), "logout");
   }

   /**
    * 切维度 / 重生 / 登出时清掉排队中的射击、共享冷却与按 UUID 缓存的轮盘快照。
    * （已经预付的法力不退——预付制，见 CHANGES。）
    */
   private static void resetVolley(Player player, String reason) {
      if (player == null || player.m_9236_().m_5776_()) {
         return;
      }

      boolean hadPending = DanmakuHandler.hasPending(player.m_20148_());
      DanmakuHandler.clearQueue(player.m_20148_());
      StaffCtrlState.clear(player.m_20148_());
      GrimoireRestrictionHandler.forgetPlayer(player.m_20148_());
      WheelScanner.forgetPlayer(player.m_20148_());

      if (hadPending && RESET_LOGGED.incrementAndGet() <= RESET_LOG_LIMIT) {
         LOGGER.debug("[touhou_spell] pending volley cleared on {}", reason);
      }
   }
}

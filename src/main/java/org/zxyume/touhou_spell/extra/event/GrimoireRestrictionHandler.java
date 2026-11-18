package org.zxyume.touhou_spell.extra.event;

import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.slf4j.Logger;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;
import org.zxyume.touhou_spell.extra.WheelScanner;
import org.zxyume.touhou_spell.init.ModSpell;

/**
 * 弹幕学法术套件（火力提升 / 火力全开 / 多重弹幕 / 飞行术）只在"授予它的法术仍在法术轮盘里"时保留。
 *
 * <p>1.2.0 起"弹幕射击"不再是法术，所以这里只处理四个状态效果：
 * 对应法术连续 {@link #MISS_CHECKS} 次不在轮盘里就清掉效果；轮盘一个都扫不到时视为"未知"，
 * 绝不清状态。轮盘扫描统一由 {@link WheelScanner} 提供（同一份规则 + 10 tick 缓存）。
 */
@EventBusSubscriber(
   modid = "touhou_spell",
   bus = Bus.FORGE
)
public final class GrimoireRestrictionHandler {
   private static final Logger LOGGER = LogUtils.getLogger();
   /** 连续多少次"缺失"判定才真正处理。 */
   private static final int MISS_CHECKS = 2;
   /** 每次会话最多打印多少行诊断。 */
   private static final int LOG_LIMIT = 40;

   private static final Map<UUID, Map<String, Integer>> MISSES = new ConcurrentHashMap<>();
   private static final Set<UUID> WHEEL_LOGGED = ConcurrentHashMap.newKeySet();
   private static final AtomicInteger LOGGED = new AtomicInteger();

   private GrimoireRestrictionHandler() {
   }

   @SubscribeEvent
   public static void onPlayerTick(PlayerTickEvent event) {
      if (event.phase != Phase.END) {
         return;
      }

      Player player = event.player;
      if (player.m_9236_().m_5776_()) {
         return;
      }

      // 便宜的前置判断：没有任何弹幕学状态就不用扫轮盘
      if (!hasActiveDanmakuState(player)) {
         return;
      }

      WheelScanner.Snapshot snapshot = WheelScanner.snapshot(player);
      if (snapshot == null || !snapshot.reliable()) {
         // 数据来源不可用（curios 未就绪 / 扫描异常）：这一次不判定
         MISSES.remove(player.m_20148_());
         return;
      }

      // 1.4.1：空轮盘也是可信结论（卸下手册 → 三个效果应当被清掉）
      Set<String> wheel = snapshot.ids();

      logWheelOnce(player, wheel);
      dropIfMissing(player, wheel, TouhouSpellExtra.POWER_UP_SPELL, TouhouSpellExtra.POWER_UP_EFFECT);
      dropIfMissing(player, wheel, TouhouSpellExtra.FLY_SPELL, TouhouSpellExtra.FLY_EFFECT);
      dropIfMissing(player, wheel, ModSpell.TEARS_UP.get(), TouhouSpellExtra.MULTI_EFFECT);
   }

   /** Drops {@code effect} once {@code spell} has been unreachable for {@link #MISS_CHECKS} checks. */
   private static void dropIfMissing(Player player, Set<String> wheel, AbstractSpell spell, MobEffect effect) {
      if (spell == null || effect == null) {
         return;
      }

      String spellId = spell.getSpellId();
      if (spellId == null) {
         return;
      }

      if (wheel.contains(spellId)) {
         forgetMiss(player, spellId);
         return;
      }

      if (confirmMiss(player, spellId)) {
         log("[%s] %s not in wheel%s -> clearing effect", player.m_19879_(), spellId, describeWheel(wheel));
         clearEffect(player, effect);
      }
   }

   private static boolean confirmMiss(Player player, String spellId) {
      Map<String, Integer> counts = MISSES.computeIfAbsent(player.m_20148_(), key -> new ConcurrentHashMap<>());
      return counts.merge(spellId, 1, Integer::sum) >= MISS_CHECKS;
   }

   private static void forgetMiss(Player player, String spellId) {
      Map<String, Integer> counts = MISSES.get(player.m_20148_());
      if (counts != null) {
         counts.remove(spellId);
      }
   }

   private static void logWheelOnce(Player player, Set<String> wheel) {
      if (WHEEL_LOGGED.add(player.m_20148_())) {
         log("[%s] danmaku spell wheel = %s", player.m_19879_(), new TreeSet<>(wheel));
      }
   }

   private static String describeWheel(Set<String> wheel) {
      return LOGGED.get() > LOG_LIMIT ? "" : " (wheel=" + new TreeSet<>(wheel) + ")";
   }

   private static void log(String format, Object... args) {
      if (LOGGED.incrementAndGet() <= LOG_LIMIT) {
         LOGGER.debug("[touhou_spell] " + String.format(format, args));
      }
   }

   private static boolean hasActiveDanmakuState(Player player) {
      return hasEffect(player, TouhouSpellExtra.POWER_UP_EFFECT)
         || hasEffect(player, TouhouSpellExtra.FLY_EFFECT)
         || hasEffect(player, TouhouSpellExtra.MULTI_EFFECT);
   }

   private static boolean hasEffect(Player player, MobEffect effect) {
      return effect != null && player.m_21023_(effect);
   }

   private static void clearEffect(Player player, MobEffect effect) {
      if (hasEffect(player, effect)) {
         player.m_21195_(effect);
      }
   }

   @SubscribeEvent
   public static void onLoggedOut(PlayerLoggedOutEvent event) {
      forgetPlayer(event.getEntity().m_20148_());
   }

   /** 清掉某个玩家的全部缓存（轮盘快照 / 缺失计数 / 日志标记）。 */
   public static void forgetPlayer(UUID id) {
      if (id == null) {
         return;
      }

      WheelScanner.forgetPlayer(id);
      MISSES.remove(id);
      WHEEL_LOGGED.remove(id);
   }
}

package org.zxyume.touhou_spell.extra;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.player.Player;

/**
 * 1.4.0：玩家是否按着 Ctrl（用于"Ctrl + 右键打开更换弹幕面板"）。
 *
 * <p>Shift 有服务端同步的标志（{@code isShiftKeyDown}），Ctrl 没有 —— 只有"是否疾跑"会被同步，
 * 而站着不动按 Ctrl 不会进入疾跑。所以由客户端在按键状态变化时发一条
 * {@code ServerboundCtrlStatePacket}，这里按 UUID 记录，登出时清理。
 */
public final class StaffCtrlState {
   private static final Map<UUID, Boolean> CTRL = new ConcurrentHashMap<>();

   private StaffCtrlState() {
   }

   public static void set(Player player, boolean down) {
      if (player == null) {
         return;
      }

      if (down) {
         CTRL.put(player.m_20148_(), Boolean.TRUE);
      } else {
         CTRL.remove(player.m_20148_());
      }
   }

   public static boolean isCtrlDown(Player player) {
      return player != null && CTRL.containsKey(player.m_20148_());
   }

   public static void clear(UUID id) {
      if (id != null) {
         CTRL.remove(id);
      }
   }
}

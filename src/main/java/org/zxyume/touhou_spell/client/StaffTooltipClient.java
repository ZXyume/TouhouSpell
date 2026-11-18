package org.zxyume.touhou_spell.client;

import dev.xkmc.youkaishomecoming.content.item.danmaku.DanmakuItem;
import dev.xkmc.youkaishomecoming.content.item.danmaku.LaserItem;
import io.redspace.ironsspellbooks.api.util.Utils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.zxyume.touhou_spell.capabilities.DanmakuHandler;
import org.zxyume.touhou_spell.capabilities.DanmakuManager;

/**
 * 万花币的物品提示（2.0.0）。
 *
 * <p>主炮（名称 / 伤害）、子机（名称 / 数量 / 伤害），最后是 Ctrl 提示。
 * 2.0.0 删掉了"主炮数量"与"射速"两行。
 */
public final class StaffTooltipClient {
   private StaffTooltipClient() {
   }

   public static void append(ItemStack stack, List<Component> tooltip) {
      Player player = Minecraft.m_91087_().f_91074_;
      if (player == null) {
         return;
      }

      DanmakuManager.getHandler(player)
         .ifPresent(
            handler -> {
               ItemStack main = handler.getStackInSlot(DanmakuHandler.MAIN_SLOT);
               ItemStack option = handler.getStackInSlot(DanmakuHandler.OPTION_SLOT);

               tooltip.add(
                  Component.m_237115_("ui.touhou_spell.main_cannon").m_130946_(": ").m_7220_(itemName(main)).m_130940_(ChatFormatting.YELLOW)
               );
               if (!main.m_41619_()) {
                  tooltip.add(
                     Component.m_237110_("ui.touhou_spell.main_cannon_damage_value", new Object[]{Utils.stringTruncation((double)itemDamage(main), 2)})
                        .m_130940_(ChatFormatting.WHITE)
                  );
               }

               tooltip.add(
                  Component.m_237115_("ui.touhou_spell.option").m_130946_(": ").m_7220_(itemName(option)).m_130940_(ChatFormatting.LIGHT_PURPLE)
               );
               if (!option.m_41619_()) {
                  tooltip.add(
                     Component.m_237110_("ui.touhou_spell.option_damage", new Object[]{Utils.stringTruncation((double)(itemDamage(option) / 2.0F), 2)})
                        .m_130940_(ChatFormatting.WHITE)
                  );
               }

            }
         );

      tooltip.add(Component.m_237115_("ui.touhou_spell.shift").m_130940_(ChatFormatting.GRAY));
   }

   private static MutableComponent itemName(ItemStack stack) {
      return stack.m_41619_() ? Component.m_237115_("ui.touhou_spell.empty").m_130940_(ChatFormatting.DARK_GRAY) : stack.m_41786_().m_6881_();
   }

   private static float itemDamage(ItemStack stack) {
      if (stack.m_41720_() instanceof DanmakuItem item) {
         return (float)item.type.damage();
      }

      return stack.m_41720_() instanceof LaserItem item ? (float)item.type.damage() : 0.0F;
   }
}

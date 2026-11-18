package org.zxyume.touhou_spell.client;

import dev.xkmc.youkaishomecoming.content.capability.GrazeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;
import org.zxyume.touhou_spell.extra.item.KaleidoCoinStaffItem;

/**
 * 1.4.0：弹幕战中被禁用时，万花币的物品格背景显示半透明红色（与 YHC 的
 * {@code DanmakuItemDeco} 完全一致的 0x7FFF0000）。
 */
public class KaleidoCoinItemDeco implements IItemDecorator {
   /** YHC 用的就是这个值（2147418112 = 0x7FFF0000）。 */
   private static final int FORBIDDEN_TINT = 0x7FFF0000;

   public boolean render(GuiGraphics graphics, Font font, ItemStack stack, int xOffset, int yOffset) {
      if (!(stack.m_41720_() instanceof KaleidoCoinStaffItem)) {
         return false;
      }

      Player player = Minecraft.m_91087_().f_91074_;
      if (player == null || !GrazeHelper.forbidDanmaku(player)) {
         return false;
      }

      graphics.m_280509_(xOffset, yOffset, xOffset + 16, yOffset + 16, FORBIDDEN_TINT);
      return true;
   }
}

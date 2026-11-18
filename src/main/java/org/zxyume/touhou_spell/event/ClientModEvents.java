package org.zxyume.touhou_spell.event;

import dev.xkmc.youkaishomecoming.content.item.danmaku.DanmakuItem;
import dev.xkmc.youkaishomecoming.content.item.danmaku.LaserItem;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.zxyume.touhou_spell.capabilities.DanmakuHandler;
import org.zxyume.touhou_spell.util.DanmakuCategory;

/**
 * 弹种物品提示（2.0.0）：只显示弹速与耗蓝。
 *
 * <p>2.0.0 删掉了"弹幕射速"，耗蓝也统一成"弹种自身伤害值"（长激光固定 40）。
 */
@EventBusSubscriber(
   modid = "touhou_spell",
   value = {Dist.CLIENT},
   bus = Bus.FORGE
)
public class ClientModEvents {
   @SubscribeEvent
   public static void onItemTooltip(ItemTooltipEvent event) {
      ItemStack stack = event.getItemStack();
      if (stack.m_41720_() instanceof DanmakuItem danmakuItem) {
         List<Component> tooltip = event.getToolTip();
         DanmakuCategory cat = DanmakuCategory.fromBullet(danmakuItem.type);
         tooltip.add(Component.m_237115_("ui.touhou_spell.speed_mod").m_130946_(": " + String.format("%.1fx", cat.speedMod)).m_130940_(ChatFormatting.BLUE));
         tooltip.add(manaLine(DanmakuHandler.bulletCost(stack, 1.0)));
      }

      if (stack.m_41720_() instanceof LaserItem laserItem) {
         List<Component> tooltip = event.getToolTip();
         DanmakuCategory cat = DanmakuCategory.fromLaser(laserItem.type);
         tooltip.add(Component.m_237115_("ui.touhou_spell.speed_mod").m_130946_(": " + String.format("%.1fx", cat.speedMod)).m_130940_(ChatFormatting.BLUE));
         tooltip.add(manaLine(DanmakuHandler.bulletCost(stack, 1.0)));
      }
   }

   private static Component manaLine(double cost) {
      return Component.m_237115_("ui.touhou_spell.mana_cost")
         .m_130946_(": ")
         .m_130946_(org.zxyume.touhou_spell.extra.SpellMath.format(cost))
         .m_130940_(ChatFormatting.BLUE);
   }
}

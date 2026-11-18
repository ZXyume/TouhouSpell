package org.zxyume.touhou_spell.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.zxyume.touhou_spell.extra.PowerPoints;
import org.zxyume.touhou_spell.extra.item.KaleidoCoinStaffItem;

/**
 * 2.1.4：手持万花币时在左上角显示 P 点（与 TLM 手执御币时的 HUD 完全同款）。
 *
 * <p>TLM 的 {@code ShowPowerEvent} 画法：P 点物品图标画在 {@code (5, 5)}，文本
 * {@code String.format("%s:%.2f", ChatFormatting.BOLD, power)} 画在 {@code (20, 10)}、颜色 {@code 0xFFFFFF}。
 * 这里照抄，只是把触发条件换成"主手或副手是万花币"。
 */
@EventBusSubscriber(
   modid = "touhou_spell",
   value = {Dist.CLIENT},
   bus = EventBusSubscriber.Bus.FORGE
)
public final class PowerHudOverlay {
   private static final String MOD_ID = "touhou_little_maid";
   private static final int ICON_X = 5;
   private static final int ICON_Y = 5;
   private static final int TEXT_X = 20;
   private static final int TEXT_Y = 10;
   private static final int TEXT_COLOR = 0xFFFFFF;

   private PowerHudOverlay() {
   }

   @SubscribeEvent
   public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
      if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) {
         return;
      }

      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (player == null || !holdingStaff(player)) {
         return;
      }

      if (!ModList.get().isLoaded(MOD_ID)) {
         return;
      }

      try {
         Hud.draw(event.getGuiGraphics(), Minecraft.m_91087_().f_91062_, PowerPoints.of(player));
      } catch (Throwable throwable) {
         // TLM 缺席 / 能力未就绪：静默跳过，不影响界面
      }
   }

   private static boolean holdingStaff(LocalPlayer player) {
      return player.m_21205_().m_41720_() instanceof KaleidoCoinStaffItem
         || player.m_21206_().m_41720_() instanceof KaleidoCoinStaffItem;
   }

   /** 隔离 TLM 类型：只有真的装了 TLM 才会加载这个类。 */
   private static final class Hud {
      private static ItemStack icon;

      private static void draw(GuiGraphics graphics, Font font, float power) {
         if (icon == null) {
            icon = com.github.tartaricacid.touhoulittlemaid.init.InitItems.POWER_POINT.get().m_7968_();
         }

         if (icon != null && !icon.m_41619_()) {
            graphics.m_280480_(icon, ICON_X, ICON_Y);
         }

         graphics.m_280488_(font, String.format("%s:%.2f", ChatFormatting.BOLD, power), TEXT_X, TEXT_Y, TEXT_COLOR);
         RenderSystem.enableBlend();
      }
   }
}

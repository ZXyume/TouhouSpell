package org.zxyume.touhou_spell.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DanmakuScreen extends AbstractContainerScreen<DanmakuMenu> {
   private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("touhou_spell", "textures/gui/danmaku_ammo.png");

   public DanmakuScreen(DanmakuMenu menu, Inventory inv, Component title) {
      super(menu, inv, title);
   }

   protected void m_7286_(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
      int x = (this.f_96543_ - this.f_97726_) / 2;
      int y = (this.f_96544_ - this.f_97727_) / 2;
      graphics.m_280218_(TEXTURE, x, y, 0, 0, this.f_97726_, this.f_97727_);
   }

   public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
      this.m_280273_(graphics);
      super.m_88315_(graphics, mouseX, mouseY, partialTicks);
      this.m_280072_(graphics, mouseX, mouseY);
   }
}

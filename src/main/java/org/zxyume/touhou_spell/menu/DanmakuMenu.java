package org.zxyume.touhou_spell.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import org.zxyume.touhou_spell.capabilities.DanmakuProvider;
import org.zxyume.touhou_spell.init.ModMenus;

public class DanmakuMenu extends AbstractContainerMenu {
   public DanmakuMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
      this(id, inv);
   }

   public DanmakuMenu(int id, Inventory inv) {
      super((MenuType)ModMenus.DANMAKU_MENU.get(), id);
      Player player = inv.f_35978_;
      player.getCapability(DanmakuProvider.CAPABILITY).ifPresent(handler -> {
         this.m_38897_(new DanmakuMenu.MegaSlot(handler, 0, 60, 35));
         this.m_38897_(new DanmakuMenu.MegaSlot(handler, 1, 100, 35));
      });
      this.addPlayerInventory(inv);
      this.addPlayerHotbar(inv);
   }

   /** 关闭面板时把弹种同步给客户端，保证 tooltip / option 圆环立刻使用新弹种。 */
   public void m_6877_(Player player) {
      super.m_6877_(player);
      org.zxyume.touhou_spell.event.CapabilityEvent.syncToClient(player);
   }

   public ItemStack m_7648_(Player player, int index) {
      return ItemStack.f_41583_;
   }

   public boolean m_6875_(Player player) {
      return true;
   }

   private void addPlayerInventory(Inventory playerInventory) {
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.m_38897_(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }
   }

   private void addPlayerHotbar(Inventory playerInventory) {
      for (int i = 0; i < 9; i++) {
         this.m_38897_(new Slot(playerInventory, i, 8 + i * 18, 142));
      }
   }

   private static class MegaSlot extends SlotItemHandler {
      public MegaSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
         super(itemHandler, index, xPosition, yPosition);
      }

      public int m_6641_() {
         return 1;
      }

      public int m_5866_(@NotNull ItemStack stack) {
         return 1;
      }
   }
}

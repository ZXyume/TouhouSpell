package org.zxyume.touhou_spell.mixin;

import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu;
import io.redspace.ironsspellbooks.item.SpellSlotUpgradeItem;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.zxyume.touhou_spell.extra.item.DanmakuGrimoireItem;

/**
 * 2.0.5：弹幕学手册不允许用「法术槽升级」扩容。
 *
 * <p>ISS 的奥术铁砧（{@code ArcaneAnvilMenu}）在升级法术槽时用
 * {@code container.getMaxSpellCount() < upgrade.maxSlots()} 判断能否再加一槽
 * （{@link SpellSlotUpgradeItem#maxSlots()} 默认是 12）。这里在铁砧输入里出现
 * {@link DanmakuGrimoireItem} 时把 {@code maxSlots()} 压到手册的固定槽位数，
 * 使该判断永远不成立 —— 手册槽位上限恒为 {@link DanmakuGrimoireItem#SPELL_SLOTS}，
 * 也不会白白消耗升级物。
 */
@Mixin(ArcaneAnvilMenu.class)
public abstract class ArcaneAnvilGrimoireMixin {
   @Redirect(
      method = "*",
      at = @At(
         value = "INVOKE",
         target = "Lio/redspace/ironsspellbooks/item/SpellSlotUpgradeItem;maxSlots()I"
      )
   )
   private int touhou_spell$capSlotUpgradeForGrimoire(SpellSlotUpgradeItem upgrade) {
      int max = upgrade.maxSlots();
      return this.touhou_spell$hasGrimoire() ? Math.min(max, DanmakuGrimoireItem.SPELL_SLOTS) : max;
   }

   private boolean touhou_spell$hasGrimoire() {
      ArcaneAnvilMenu menu = (ArcaneAnvilMenu)(Object)this;
      for (Slot slot : menu.f_38839_) {
         if (slot != null && slot.m_7993_().m_41720_() instanceof DanmakuGrimoireItem) {
            return true;
         }
      }

      return false;
   }
}

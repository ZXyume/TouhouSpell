package org.zxyume.touhou_spell.capabilities;

import com.mojang.logging.LogUtils;
import dev.xkmc.youkaishomecoming.content.item.danmaku.DanmakuItem;
import dev.xkmc.youkaishomecoming.init.registrate.YHDanmaku.Bullet;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class DanmakuProvider implements ICapabilitySerializable<CompoundTag> {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final Capability<DanmakuHandler> CAPABILITY = CapabilityManager.get(new CapabilityToken<DanmakuHandler>() {
   });
   private final DanmakuHandler handler = new DanmakuHandler();
   private final LazyOptional<DanmakuHandler> optional = LazyOptional.of(() -> this.handler);

   public DanmakuProvider() {
      this.handler.setStackInSlot(0, defaultAmmo());
   }

   /**
    * 默认弹种：红色 CIRCLE。1.1.2 起整段做了空值/类型保护 ——
    * YHC 更换枚举或某发弹幕被配置禁用时，不能再让能力挂载阶段抛 NPE。
    */
   private static ItemStack defaultAmmo() {
      try {
         Item item = Bullet.CIRCLE.get(DyeColor.RED).get();
         if (item instanceof DanmakuItem) {
            return new ItemStack(item, 1);
         }
      } catch (Throwable throwable) {
         LOGGER.debug("[touhou_spell] could not build the default danmaku ammo", throwable);
      }

      return ItemStack.f_41583_;
   }

   @NotNull
   public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
      return CAPABILITY.orEmpty(cap, this.optional);
   }

   public CompoundTag serializeNBT() {
      return this.handler.serializeNBT();
   }

   public void deserializeNBT(CompoundTag nbt) {
      this.handler.deserializeNBT(nbt);
   }
}

package org.zxyume.touhou_spell.extra.item;

import com.mojang.logging.LogUtils;
import dev.xkmc.youkaishomecoming.content.capability.GrazeHelper;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.item.weapons.AttributeContainer;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import io.redspace.ironsspellbooks.item.weapons.StaffTier;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.zxyume.touhou_spell.capabilities.DanmakuManager;
import org.zxyume.touhou_spell.client.StaffTooltipClient;
import org.zxyume.touhou_spell.extra.StaffCtrlState;
import org.zxyume.touhou_spell.menu.DanmakuMenu;

/**
 * 万花币（Kaleido Coin）——弹幕学的发射装置（1.2.0）。
 *
 * <p>1.2.0 起"弹幕射击"不再是法术：万花币本身没有法术容器、也不可更换任何东西（对齐
 * Staff of the Nines 的"不可替换"定位），右键直接发射一轮弹幕：
 *
 * <ul>
 *   <li>右键：触发一次齐射（主炮 + 子机，法力一次性预付）；</li>
 *   <li>Ctrl + 右键：打开「更换弹幕」面板（不受冷却/禁射限制）；</li>
 *   <li>所有万花币共享同一个原版物品冷却（{@code ItemCooldowns} 以 Item 为键）；</li>
 *   <li>处于妖怪们的归家"弹幕战被禁用"状态（{@link GrazeHelper#forbidDanmaku}）时无法发射；</li>
 *   <li>属性仍保留"弹幕学强度 +15%、法力值恢复 +30%"。</li>
 * </ul>
 */
public class KaleidoCoinStaffItem extends StaffItem {
   /**
    * 自定义稀有度：物品名称默认显示为品红色（MC 的 LIGHT_PURPLE = #FF55FF，也就是品红）。
    *
    * <p>懒创建 + 兜底：万一某个环境里枚举扩展不可用，就退回 {@code Rarity.RARE}，
    * 宁可颜色不对也不要启动就崩。
    */
   private static final Logger LOGGER = LogUtils.getLogger();
   private static Rarity kaleidoRarity;

   public static Rarity kaleidoRarity() {
      if (kaleidoRarity == null) {
         try {
            kaleidoRarity = Rarity.create("KALEIDO_COIN", ChatFormatting.LIGHT_PURPLE);
         } catch (Throwable throwable) {
            LOGGER.debug("[touhou_spell] could not create the KALEIDO_COIN rarity, falling back to RARE", throwable);
            kaleidoRarity = Rarity.RARE;
         }
      }

      return kaleidoRarity;
   }

   /** 法力值恢复 +30%。 */
   public static final double MANA_REGEN = 0.30;
   /**
    * 近战攻击力（属性修正值）。物品提示里显示的是「玩家基础 1 + 该值」，
    * 所以 4.0F 在游戏里显示为 **5 攻击伤害**。
    */
   public static final float ATTACK_DAMAGE = 4.0F;
   /**
    * 近战攻速（属性修正值）。玩家基础攻速 4.0，游戏里显示 4.0 + 该值，
    * 所以 -2.0F 显示为 **2 攻击速度**。
    */
   public static final float ATTACK_SPEED = -2.0F;

   public KaleidoCoinStaffItem(Properties properties) {
      super(
         properties,
         new StaffTier(
            ATTACK_DAMAGE,
            ATTACK_SPEED,
            new AttributeContainer(
               () -> (Attribute)AttributeRegistry.MANA_REGEN.get(), MANA_REGEN, Operation.MULTIPLY_BASE
            )
         )
      );
   }

   public InteractionResultHolder<ItemStack> m_7203_(Level level, Player player, InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (level.m_5776_()) {
         return InteractionResultHolder.m_19090_(stack);
      }

      if (!(player instanceof ServerPlayer serverPlayer)) {
         return InteractionResultHolder.m_19100_(stack);
      }

      // Ctrl + 右键：打开「更换弹幕」面板（不受冷却 / 禁射限制）
      if (StaffCtrlState.isCtrlDown(serverPlayer)) {
         NetworkHooks.openScreen(
            serverPlayer, new SimpleMenuProvider((id, inv, p) -> new DanmakuMenu(id, inv), Component.m_237115_("ui.touhou_spell.danmaku_ammo"))
         );
         return InteractionResultHolder.m_19090_(stack);
      }

      // 禁射（弹幕战中被禁用）时与 YHC 的弹幕/激光/符卡一致：完全静默，不弹任何文字
      if (GrazeHelper.forbidDanmaku(player)) {
         return InteractionResultHolder.m_19100_(stack);
      }

      // 冷却用原版物品冷却（ItemCooldowns 以 Item 为键，因此所有万花币天然共享同一个冷却），
      // 表现与末影珍珠一致：物品格上的冷却遮罩，不再弹文字提示。
      if (player.m_36335_().m_41519_(this)) {
         return InteractionResultHolder.m_19090_(stack);
      }

      int cooldown = DanmakuManager.getHandler(player)
         .map(handler -> handler.fireVolley(serverPlayer, MagicData.getPlayerMagicData(player)))
         .orElse(0);
      if (cooldown <= 0) {
         return InteractionResultHolder.m_19098_(stack);
      }

      player.m_36335_().m_41524_(this, cooldown);
      return InteractionResultHolder.m_19090_(stack);
   }

   public void m_7373_(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
      // 弹种 / 伤害 / 子机数量 / 冷却 + Ctrl 提示：数据在客户端能力里，交给 client 侧工具类拼装。
      DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> StaffTooltipClient.append(stack, tooltip));
      super.m_7373_(stack, level, tooltip, flag);
   }
}

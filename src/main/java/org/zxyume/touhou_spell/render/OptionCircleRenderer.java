package org.zxyume.touhou_spell.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xkmc.fastprojectileapi.spellcircle.SpellCircleConfig;
import dev.xkmc.fastprojectileapi.spellcircle.SpellComponent;
import dev.xkmc.fastprojectileapi.spellcircle.SpellRenderState;
import dev.xkmc.fastprojectileapi.spellcircle.SpellComponent.RenderHandle;
import dev.xkmc.youkaishomecoming.init.YoukaisHomecoming;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.zxyume.touhou_spell.Touhou_spell;
import org.zxyume.touhou_spell.capabilities.DanmakuHandler;
import org.zxyume.touhou_spell.capabilities.DanmakuManager;
import org.zxyume.touhou_spell.extra.SpellMath;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;
import org.zxyume.touhou_spell.extra.WheelLevels;
import org.zxyume.touhou_spell.init.ModAttribute;
import org.zxyume.touhou_spell.init.ModSpell;
import org.zxyume.touhou_spell.util.DanmakuMath;

public class OptionCircleRenderer {
   public static final ResourceLocation DEFAULT_CIRCLE = Touhou_spell.loc("test_spell_0");
   private static final ResourceLocation CIRCLE_TEX = YoukaisHomecoming.loc("textures/entities/spell_circle.png");

   public static void renderOptions(PoseStack pose, MultiBufferSource buffer, int light, Player player, float pTick) {
      DanmakuManager.getHandler(player).ifPresent(handler -> {
         ItemStack subStack = handler.getStackInSlot(1);
         // 1.4.2：效果关闭时按 0 级处理（与 DanmakuHandler 的服务端判定同源）
         int extraOptions = Math.max(0, (int)Math.round(
            player.m_21133_((net.minecraft.world.entity.ai.attributes.Attribute)ModAttribute.D_OPTION.get())) - 1);
         int multiLevel = player.m_21023_(TouhouSpellExtra.MULTI_EFFECT)
            ? WheelLevels.activeLevel(player, ModSpell.TEARS_UP.get())
            : 0;

         int count = SpellMath.optionCount(multiLevel, extraOptions);
         if (!subStack.m_41619_() && count > 0) {
            SpellComponent component = SpellCircleConfig.getFromConfig(DEFAULT_CIRCLE);
            if (component != null) {
               Quaternionf cameraRotation = Minecraft.m_91087_().m_91290_().m_253208_();
               Vec3 camPos = Minecraft.m_91087_().f_91063_.m_109153_().m_90583_();

               for (int i = 0; i < count; i++) {
                  Vec3 pos = DanmakuMath.getOptionPos(player, i, count, player.f_19797_, pTick);
                  pose.m_85836_();
                  pose.m_85837_(pos.f_82479_ - camPos.f_82479_, pos.f_82480_ - camPos.f_82480_, pos.f_82481_ - camPos.f_82481_);
                  float baseScale = 0.05F;
                  pose.m_85841_(baseScale, baseScale, baseScale);
                  if (cameraRotation != null) {
                     pose.m_252781_(cameraRotation);
                     pose.m_252781_(Axis.f_252436_.m_252977_(180.0F));
                  }

                  RenderHandle handle = new RenderHandle(pose, buffer.m_6299_(SpellRenderState.getSpell(CIRCLE_TEX)), (float)player.f_19797_ + pTick, light);
                  component.render(handle);
                  pose.m_85849_();
               }
            }
         }
      });
   }
}

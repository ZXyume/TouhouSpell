package org.zxyume.touhou_spell.extra;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.zxyume.touhou_spell.Touhou_spell;

/**
 * 弹幕学内容的共享常量与"当前实例"引用。
 *
 * <p>1.1.2 起注册工作全部搬到 {@code init} 包（{@link org.zxyume.touhou_spell.init.ModItem}、
 * {@code ModSpell}、{@code ModEffect}），这里只保留：
 * <ul>
 *   <li>弹幕学学派 id（多个法术/属性引用它）；</li>
 *   <li>注册 lambda 回填的静态实例（{@link #POWER_UP_SPELL} 等），供运行期逻辑直接取用；</li>
 *   <li>创造标签内容（{@link #onBuildCreativeTab}）。</li>
 * </ul>
 */
@EventBusSubscriber(
   modid = "touhou_spell",
   bus = Bus.MOD
)
public final class TouhouSpellExtra {
   public static final ResourceLocation DANMAKU_SCHOOL = Touhou_spell.id("danmaku");
   private static final ResourceKey<CreativeModeTab> EQUIPMENT_TAB = ResourceKey.m_135785_(Registries.f_279569_, Touhou_spell.id("spellbook_equipment"));

   /** 由 {@code ModItem} / {@code ModEffect} / {@code ModSpell} 的注册 lambda 回填。 */
   public static Item DANMAKU_ORB;
   public static Item DANMAKU_GRIMOIRE;
   public static MobEffect POWER_UP_EFFECT;
   public static MobEffect FLY_EFFECT;
   public static MobEffect MULTI_EFFECT;
   public static AbstractSpell POWER_UP_SPELL;
   public static AbstractSpell FLY_SPELL;
   public static AbstractSpell CAST_SPELL_CARD;

   private TouhouSpellExtra() {
   }

   @SubscribeEvent
   public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
      if (event.getTabKey().equals(EQUIPMENT_TAB)) {
         if (DANMAKU_ORB != null) {
            event.accept(() -> DANMAKU_ORB);
         }

         if (DANMAKU_GRIMOIRE != null) {
            event.accept(() -> DANMAKU_GRIMOIRE);
         }
      }
   }
}

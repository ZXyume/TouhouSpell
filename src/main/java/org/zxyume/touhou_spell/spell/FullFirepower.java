package org.zxyume.touhou_spell.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.zxyume.touhou_spell.extra.SpellMath;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;
import org.zxyume.touhou_spell.init.ModSchool;

/**
 * 火力全开（2.0.0：合并了原「多重弹幕」）。
 *
 * <p>每级：子机数量 +1、主炮伤害 +25%；子机数量封顶 7 之后，每级改为子机伤害 +15%。
 * 效果只有一级（amplifier 恒 0），具体等级在发射时从轮盘现读。
 */
@AutoSpellConfig
public class FullFirepower extends AbstractSpell {
   private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath("touhou_spell", "full_firepower");
   private final DefaultConfig defaultConfig = new DefaultConfig()
      .setMinRarity(SpellRarity.LEGENDARY)
      .setSchoolResource(ModSchool.DANMAKU_RESOURCE)
      .setMaxLevel(1)
      .setCooldownSeconds(3.0)
      .build();

   public FullFirepower() {
      this.manaCostPerLevel = 0;
      this.baseSpellPower = 10;
      this.spellPowerPerLevel = 0;
      this.castTime = 0;
      this.baseManaCost = 25;
   }

   public List<MutableComponent> getUniqueInfo(int level, LivingEntity entity) {
      int effective = Math.max(1, level);
      List<MutableComponent> info = new java.util.ArrayList<>(5);
      info.add(Component.m_237110_("ui.touhou_spell.option_count", new Object[]{SpellMath.optionCount(effective)}));
      info.add(
         Component.m_237110_(
            "ui.touhou_spell.main_cannon_damage", new Object[]{SpellMath.format((SpellMath.mainDamageMultiplier(effective) - 1.0) * 100.0)}
         )
      );
      info.add(Component.m_237115_("ui.touhou_spell.cost_mana"));
      info.add(Component.m_237115_("ui.touhou_spell.toggle"));
      return info;
   }

   public CastType getCastType() {
      return CastType.INSTANT;
   }

   public DefaultConfig getDefaultConfig() {
      return this.defaultConfig;
   }

   public ResourceLocation getSpellResource() {
      return this.spellId;
   }

   public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData magicData) {
      if (!level.m_5776_() && caster instanceof Player player) {
         if (player.m_21023_(TouhouSpellExtra.MULTI_EFFECT)) {
            player.m_21195_(TouhouSpellExtra.MULTI_EFFECT);
         } else {
            player.m_7292_(new MobEffectInstance(TouhouSpellExtra.MULTI_EFFECT, 30, 0, false, false, true));
         }
      }

      super.onCast(level, spellLevel, caster, source, magicData);
   }

   public AnimationHolder getCastStartAnimation() {
      return SpellAnimations.SELF_CAST_ANIMATION;
   }

   public boolean allowLooting() {
      return false;
   }

   public boolean allowCrafting() {
      return false;
   }
}

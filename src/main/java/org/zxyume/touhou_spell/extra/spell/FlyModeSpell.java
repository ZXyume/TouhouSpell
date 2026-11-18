package org.zxyume.touhou_spell.extra.spell;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;

public class FlyModeSpell extends AbstractSpell {
   public static final ResourceLocation ID = new ResourceLocation("touhou_spell", "flymode");
   /** Levels above this add nothing: the mana drain is already at its floor. */
   public static final int MAX_EFFECT_LEVEL = 3;
   private final DefaultConfig defaultConfig = new DefaultConfig()
      .setMinRarity(SpellRarity.LEGENDARY)
      .setSchoolResource(TouhouSpellExtra.DANMAKU_SCHOOL)
      .setMaxLevel(2)
      .setCooldownSeconds(3.0)
      .build();

   public FlyModeSpell() {
      this.baseManaCost = 10;
      this.manaCostPerLevel = 0;
      this.baseSpellPower = 0;
      this.spellPowerPerLevel = 0;
      this.castTime = 0;
   }

   public ResourceLocation getSpellResource() {
      return ID;
   }

   public DefaultConfig getDefaultConfig() {
      return this.defaultConfig;
   }

   public CastType getCastType() {
      return CastType.INSTANT;
   }

   public SchoolType getSchoolType() {
      SchoolType var1 = SchoolRegistry.getSchool(TouhouSpellExtra.DANMAKU_SCHOOL);
      return var1 != null ? var1 : (SchoolType)SchoolRegistry.ENDER.get();
   }

   public boolean requiresLearning() {
      return false;
   }

   public boolean allowCrafting() {
      return false;
   }

   public List<MutableComponent> getUniqueInfo(int var1, LivingEntity var2) {
      return List.of(Component.m_237115_("spell.touhou_spell.flymode.info"), Component.m_237115_("spell.touhou_spell.flymode.toggle"));
   }

   public void onCast(Level var1, int var2, LivingEntity var3, CastSource var4, MagicData var5) {
      if (!var1.m_5776_() && var3 instanceof Player var6) {
         if (var6.m_21023_(TouhouSpellExtra.FLY_EFFECT)) {
            var6.m_21195_(TouhouSpellExtra.FLY_EFFECT);
         } else {
            // 1.4.1：效果只有一级（amplifier 恒 0），飞行术等级改为耗蓝结算时从轮盘等级现算
            var6.m_7292_(new MobEffectInstance(TouhouSpellExtra.FLY_EFFECT, 30, 0, false, false, true));
         }
      }

      super.onCast(var1, var2, var3, var4, var5);
   }
}

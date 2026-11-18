package org.zxyume.touhou_spell.extra.spell;

import dev.xkmc.youkaishomecoming.content.item.danmaku.ISpellItem;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.spells.CastResult.Type;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;
import org.zxyume.touhou_spell.extra.event.SpellCostHandler;
import org.zxyume.touhou_spell.extra.util.CardUtil;

public class CastSpellCardSpell extends AbstractSpell {
   private static final ResourceLocation SPELL_ID = new ResourceLocation("touhou_spell", "cast_spell_card");
   private final DefaultConfig defaultConfig = new DefaultConfig()
      .setMinRarity(SpellRarity.LEGENDARY)
      .setSchoolResource(TouhouSpellExtra.DANMAKU_SCHOOL)
      .setMaxLevel(1)
      .setCooldownSeconds(0.0)
      .build();

   public CastSpellCardSpell() {
      this.baseManaCost = SpellCostHandler.SPELL_CARD_MANA_COST;
      this.manaCostPerLevel = 0;
      this.baseSpellPower = 0;
      this.spellPowerPerLevel = 0;
      this.castTime = 0;
   }

   public ResourceLocation getSpellResource() {
      return SPELL_ID;
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
      return List.of(Component.m_237115_("spell.touhou_spell.cast_spell_card.info"), Component.m_237115_("spell.touhou_spell.cast_spell_card.info2"));
   }

   public CastResult canBeCastedBy(int var1, CastSource var2, MagicData var3, Player var4) {
      CastResult var5 = super.canBeCastedBy(var1, var2, var3, var4);
      if (!var5.isSuccess()) {
         return var5;
      } else {
         ItemStack var6 = CardUtil.getCard(var4);
         if (var6.m_41619_()) {
            return fail("message.touhou_spell.no_card");
         } else {
            Item var7 = var6.m_41720_();
            if (!(var7 instanceof ISpellItem)) {
               return fail("message.touhou_spell.not_spell_card");
            } else {
               return CardUtil.isOnCooldown(var4, var7) ? fail("message.touhou_spell.cooldown") : var5;
            }
         }
      }
   }

   private static CastResult fail(String var0) {
      return new CastResult(Type.FAILURE, Component.m_237115_(var0).m_130940_(ChatFormatting.RED));
   }

   public void onCast(Level var1, int var2, LivingEntity var3, CastSource var4, MagicData var5) {
      // 1.2.0：妖怪们的归家"弹幕战被禁用"期间，符卡与万花币一起被禁用
      if (!var1.m_5776_()
         && var3 instanceof ServerPlayer blocked
         && dev.xkmc.youkaishomecoming.content.capability.GrazeHelper.forbidDanmaku(blocked)) {
         return;
      }

      if (!var1.m_5776_() && var3 instanceof ServerPlayer var6 && CardUtil.release(var6) && var4.consumesMana() && !var6.m_7500_()) {
         var5.setMana(Math.max(0.0F, var5.getMana() - (float)SpellCostHandler.SPELL_CARD_MANA_COST));
         PacketDistributor.sendToPlayer(var6, new SyncManaPacket(var5));
      }

      super.onCast(var1, var2, var3, var4, var5);
   }
}

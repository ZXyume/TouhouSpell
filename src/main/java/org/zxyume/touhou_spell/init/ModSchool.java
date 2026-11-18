package org.zxyume.touhou_spell.init;

import dev.xkmc.youkaishomecoming.init.registrate.YHSounds;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.zxyume.touhou_spell.Touhou_spell;
import org.zxyume.touhou_spell.util.ModTags;

public class ModSchool {
   private static final DeferredRegister<SchoolType> SCHOOLS = DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, "touhou_spell");
   public static final ResourceLocation DANMAKU_RESOURCE = Touhou_spell.id("danmaku");
   public static final RegistryObject<SchoolType> DANMAKU = registerSchool(
      new SchoolType(
         DANMAKU_RESOURCE,
         ModTags.FUSION_FOCUS,
         Component.m_237115_("school.touhou_spell.danmaku").m_130940_(ChatFormatting.GOLD),
         LazyOptional.of(ModAttribute.D_SPELL_POWER::get),
         null,
         LazyOptional.of(YHSounds.GRAZE::get),
         null
      )
   );

   private static RegistryObject<SchoolType> registerSchool(SchoolType schoolType) {
      return SCHOOLS.register(schoolType.getId().m_135815_(), () -> schoolType);
   }

   public static void register(IEventBus eventBus) {
      SCHOOLS.register(eventBus);
   }
}

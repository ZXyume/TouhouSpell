package org.zxyume.touhou_spell.init;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.zxyume.touhou_spell.Touhou_spell;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;
import org.zxyume.touhou_spell.extra.spell.CastSpellCardSpell;
import org.zxyume.touhou_spell.extra.spell.FlyModeSpell;
import org.zxyume.touhou_spell.extra.spell.PowerUpSpell;
import org.zxyume.touhou_spell.spell.FullFirepower;

/**
 * 法术注册的唯一入口（1.1.2 起）。原先 {@code power_up / flymode / multi_danmaku / cast_spell_card}
 * 走 {@link TouhouSpellExtra} 里的裸 {@code RegisterEvent}，另外三个走 DeferredRegister，
 * 两套风格并存；现在统一在这里注册。
 *
 * <p>{@link TouhouSpellExtra} 上的静态字段仍由注册 lambda 赋值，保持所有既有引用可用。
 */
public class ModSpell {
   private static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, Touhou_spell.MODID);

   /** 2.0.0：火力全开（已合并原多重弹幕）；沿用 {@code TEARS_UP} 这个字段名以保持既有引用。 */
   public static final RegistryObject<AbstractSpell> TEARS_UP = SPELLS.register("full_firepower", FullFirepower::new);

   public static final RegistryObject<AbstractSpell> POWER_UP = SPELLS.register(
      "power_up", () -> TouhouSpellExtra.POWER_UP_SPELL = new PowerUpSpell()
   );
   public static final RegistryObject<AbstractSpell> FLY = SPELLS.register(
      "flymode", () -> TouhouSpellExtra.FLY_SPELL = new FlyModeSpell()
   );
   public static final RegistryObject<AbstractSpell> CAST_SPELL_CARD = SPELLS.register(
      "cast_spell_card", () -> TouhouSpellExtra.CAST_SPELL_CARD = new CastSpellCardSpell()
   );

   public static void register(IEventBus eventBus) {
      SPELLS.register(eventBus);
   }
}

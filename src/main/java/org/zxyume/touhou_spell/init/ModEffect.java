package org.zxyume.touhou_spell.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.zxyume.touhou_spell.Touhou_spell;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;
import org.zxyume.touhou_spell.extra.effect.FlyModeEffect;
import org.zxyume.touhou_spell.extra.effect.MultiDanmakuEffect;
import org.zxyume.touhou_spell.extra.effect.PowerUpEffect;

/**
 * 状态效果注册的唯一入口。
 *
 * <p>2.0.0 起只剩三个效果：火力提升 / 火力全开（合并了原多重弹幕）/ 飞行术。
 */
public class ModEffect {
   public static final DeferredRegister<MobEffect> MOB_EFFECT_DEFERRED_REGISTER = DeferredRegister.create(Registries.f_256929_, Touhou_spell.MODID);

   public static final RegistryObject<MobEffect> POWER_UP = MOB_EFFECT_DEFERRED_REGISTER.register(
      "power_up", () -> TouhouSpellExtra.POWER_UP_EFFECT = new PowerUpEffect()
   );
   public static final RegistryObject<MobEffect> FLY_MODE = MOB_EFFECT_DEFERRED_REGISTER.register(
      "flymode", () -> TouhouSpellExtra.FLY_EFFECT = new FlyModeEffect()
   );
   public static final RegistryObject<MobEffect> FULL_FIREPOWER = MOB_EFFECT_DEFERRED_REGISTER.register(
      "full_firepower", () -> TouhouSpellExtra.MULTI_EFFECT = new MultiDanmakuEffect()
   );

   public static void register(IEventBus eventBus) {
      MOB_EFFECT_DEFERRED_REGISTER.register(eventBus);
   }
}

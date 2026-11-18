package org.zxyume.touhou_spell.init;

import io.redspace.ironsspellbooks.registries.UpgradeOrbTypeRegistry;
import io.redspace.ironsspellbooks.util.ItemPropertiesHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.zxyume.touhou_spell.Touhou_spell;
import org.zxyume.touhou_spell.compat.DurabilityCompat;
import org.zxyume.touhou_spell.extra.TouhouSpellExtra;
import org.zxyume.touhou_spell.extra.item.DanmakuGrimoireItem;
import org.zxyume.touhou_spell.extra.item.ExtraUpgradeOrbItem;
import org.zxyume.touhou_spell.extra.item.KaleidoCoinStaffItem;
import org.zxyume.touhou_spell.item.armor.MaidArmorItem;

/**
 * 物品注册的唯一入口（1.1.2 起）。原先 {@code danmaku_upgrade_orb} / {@code danmaku_grimoire}
 * 在 {@link TouhouSpellExtra} 里用裸 {@code RegisterEvent} 注册，和其它物品分居两处，容易漏模型 /
 * 漏创造标签，现在统一到这里。
 *
 * <p>{@link TouhouSpellExtra#DANMAKU_ORB} / {@link TouhouSpellExtra#DANMAKU_GRIMOIRE} 这两个静态字段
 * 仍在注册 lambda 里赋值，保持既有引用（创造标签、客户端渲染器注册）不变。
 */
public class ModItem {
   public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Touhou_spell.MODID);

   public static final RegistryObject<Item> MAID_CHESTPLATE = ITEMS.register(
      "maid_chestplate", () -> new MaidArmorItem(Type.CHESTPLATE, DurabilityCompat.armor(ItemPropertiesHelper.equipment().m_41497_(Rarity.EPIC)))
   );
   public static final RegistryObject<Item> MAID_LEGGINGS = ITEMS.register(
      "maid_leggings", () -> new MaidArmorItem(Type.LEGGINGS, DurabilityCompat.armor(ItemPropertiesHelper.equipment().m_41497_(Rarity.EPIC)))
   );
   public static final RegistryObject<Item> MAID_BOOTS = ITEMS.register(
      "maid_boots", () -> new MaidArmorItem(Type.BOOTS, DurabilityCompat.armor(ItemPropertiesHelper.equipment().m_41497_(Rarity.EPIC)))
   );

   /** 万花币：铁魔法法杖体系里的施法武器，固有 locked「弹幕射击」。 */
   public static final RegistryObject<Item> KALEIDO_COIN = ITEMS.register(
      "kaleido_coin",
      () -> new KaleidoCoinStaffItem(ItemPropertiesHelper.equipment(1).m_41497_(KaleidoCoinStaffItem.kaleidoRarity()).m_41486_())
   );

   /** 弹幕学升级球（掉落表 / 合成由 datapack 定义）。 */
   public static final RegistryObject<Item> DANMAKU_ORB = ITEMS.register(
      "danmaku_upgrade_orb",
      () -> TouhouSpellExtra.DANMAKU_ORB = new ExtraUpgradeOrbItem(
         ItemPropertiesHelper.material().m_41497_(Rarity.UNCOMMON),
         ResourceKey.m_135785_(UpgradeOrbTypeRegistry.UPGRADE_ORB_REGISTRY_KEY, Touhou_spell.id("danmaku"))
      )
   );

   /** 弹幕学手册：4 个 locked 固有法术，放在 spellbook 饰品槽里生效。 */
   public static final RegistryObject<Item> DANMAKU_GRIMOIRE = ITEMS.register(
      "danmaku_grimoire",
      () -> TouhouSpellExtra.DANMAKU_GRIMOIRE = new DanmakuGrimoireItem(
         ItemPropertiesHelper.equipment(1).m_41497_(Rarity.RARE).m_41486_()
      )
   );

   public static void register(IEventBus eventBus) {
      ITEMS.register(eventBus);
   }
}

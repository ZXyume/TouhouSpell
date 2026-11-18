package org.zxyume.touhou_spell.util;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.zxyume.touhou_spell.Touhou_spell;

/**
 * 本模组用到的标签。
 *
 * <p>1.1.2：删掉了从未被调用、也没有对应 tag 文件的私有 create()；{@link #FUSION_FOCUS} 仍然被
 * {@code ModSchool} 用作弹幕学学派的"专注物品"标签（历史命名保持兼容）。
 */
public final class ModTags {
   /** 弹幕学学派的专注物品标签（{@code touhou_spell:gunmu_focus}）。 */
   public static final TagKey<Item> FUSION_FOCUS = ItemTags.create(Touhou_spell.id("gunmu_focus"));

   private ModTags() {
   }
}

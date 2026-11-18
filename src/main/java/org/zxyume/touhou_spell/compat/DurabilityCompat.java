package org.zxyume.touhou_spell.compat;

import net.minecraft.world.item.Item.Properties;

public final class DurabilityCompat {
   private DurabilityCompat() {
   }

   public static Properties armor(Properties var0) {
      return var0.m_41503_(225);
   }

   public static Properties axe(Properties var0) {
      return var0.m_41503_(750);
   }
}

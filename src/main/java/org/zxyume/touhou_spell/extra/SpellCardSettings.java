package org.zxyume.touhou_spell.extra;

import java.util.Map;

public final class SpellCardSettings {
   public static final int DEFAULT_COOLDOWN = 200;
   private static final Map<String, Integer> COOLDOWNS = Map.of(
      "youkaishomecoming:spell_reimu",
      300,
      "youkaishomecoming:spell_marisa",
      430,
      "youkaishomecoming:spell_sanae",
      400,
      "youkaishomecoming:spell_mystia",
      400,
      "youkaishomecoming:spell_koishi",
      400,
      "youkaishomecoming:spell_remilia",
      200,
      "youkaishomecoming:spell_clownpiece",
      1200,
      "youkaishomecoming:spell_yukari_laser",
      300,
      "youkaishomecoming:spell_yukari_butterfly",
      200
   );

   private SpellCardSettings() {
   }

   public static int cooldownFor(String var0) {
      return COOLDOWNS.getOrDefault(var0, DEFAULT_COOLDOWN);
   }
}

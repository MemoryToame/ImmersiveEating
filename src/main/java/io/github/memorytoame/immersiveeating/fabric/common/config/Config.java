package io.github.memorytoame.immersiveeating.fabric.common.config;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
   public static final ModConfigSpec COMMON_SPEC;
   public static final ModConfigSpec.BooleanValue ENABLE_RIGHT_BUTTON;

   static {
      ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
      builder.push("immersive_eating");
      ENABLE_RIGHT_BUTTON = builder.comment("Enable right-click animation").define("enableRightClickAnimation", true);
      builder.pop();
      COMMON_SPEC = builder.build();
   }

   private Config() {
   }

   public static void register() {
      NeoForgeConfigRegistry.INSTANCE.register("food", ModConfig.Type.COMMON, COMMON_SPEC, "immersive_eating.toml");
   }
}


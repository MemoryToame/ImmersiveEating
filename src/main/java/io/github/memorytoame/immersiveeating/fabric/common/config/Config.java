package io.github.memorytoame.immersiveeating.fabric.common.config;

import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry;
import net.minecraftforge.common.ForgeConfigSpec;
import net.neoforged.fml.config.ModConfig;

public final class Config {
   public static final ForgeConfigSpec COMMON_SPEC;
   public static final ForgeConfigSpec.BooleanValue ENABLE_RIGHT_BUTTON;

   static {
      ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
      builder.push("immersive_eating");
      ENABLE_RIGHT_BUTTON = builder.comment("Enable right-click animation").define("enableRightClickAnimation", true);
      builder.pop();
      COMMON_SPEC = builder.build();
   }

   private Config() {
   }

   public static void register() {
      ForgeConfigRegistry.INSTANCE.register("food", ModConfig.Type.COMMON, COMMON_SPEC, "immersive_eating.toml");
   }
}


package io.github.memorytoame.immersiveeating.fabric.client.config;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

@Environment(EnvType.CLIENT)
public final class ClientConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec.BooleanValue HIDE_GUI_WHEN_EATING;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("immersive_eating");
        HIDE_GUI_WHEN_EATING = builder.comment("Hide Your hotbar & status bar when eating").define("hideGUIWhenEating", true);
        builder.pop();
        CLIENT_SPEC = builder.build();
    }

    private ClientConfig() {
    }

    public static void register() {
        NeoForgeConfigRegistry.INSTANCE.register("food", ModConfig.Type.CLIENT, CLIENT_SPEC, "immersive_eating_client.toml");
    }
}

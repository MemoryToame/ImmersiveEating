package io.github.memorytoame.immersiveeating.neoforge.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final ModConfigSpec COMMON_SPEC;

    public static final ModConfigSpec.BooleanValue ENABLE_RIGHT_BUTTON;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("immersive_eating");

        ENABLE_RIGHT_BUTTON = builder.comment("是否右键启用动画").define("enableRightClickAnimation", true);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private Config() {
    }
}

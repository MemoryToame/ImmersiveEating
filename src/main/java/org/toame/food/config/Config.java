package org.toame.food.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    public static final ForgeConfigSpec COMMON_SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_RIGHT_BUTTON;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("immersive_eating");

        ENABLE_RIGHT_BUTTON = builder.comment("是否右键启用动画").define("enableRightClickAnimation", true);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private Config() {
    }
}

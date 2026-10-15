package io.github.memorytoame.immersiveeating.fabric.plugin;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

public final class Plugins implements IMixinConfigPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (targetClassName.startsWith("punchy.config")) {
            return FabricLoader.getInstance().isModLoaded("punchy");
        }

        return true;
    }
}

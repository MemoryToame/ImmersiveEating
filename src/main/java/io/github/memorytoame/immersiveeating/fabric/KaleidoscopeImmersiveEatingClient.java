package io.github.memorytoame.immersiveeating.fabric;

import io.github.memorytoame.immersiveeating.fabric.client.ClientSetup;
import io.github.memorytoame.immersiveeating.fabric.client.input.InputEvents;
import io.github.memorytoame.immersiveeating.fabric.client.input.ModKeyMappings;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class KaleidoscopeImmersiveEatingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(ModKeyMappings.ANIMATION);
        ClientSetup.register();
        InputEvents.register();
    }
}

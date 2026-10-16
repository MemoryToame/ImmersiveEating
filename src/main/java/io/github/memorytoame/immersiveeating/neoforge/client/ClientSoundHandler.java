package io.github.memorytoame.immersiveeating.neoforge.client;

import io.github.memorytoame.immersiveeating.neoforge.client.animation.ClientAnimationManager;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.StopSoundPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public final class ClientSoundHandler {
    private static final Set<ResourceLocation> activeSoundIds = new HashSet<>();

    private ClientSoundHandler() {
    }
    public static void playLocal(ResourceLocation soundId) {
        if (soundId == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        activeSoundIds.add(soundId);
        minecraft.getSoundManager().play(
                new SimpleSoundInstance(
                soundId,
                SoundSource.PLAYERS,
                0.75F,
                1.0F,
                RandomSource.create(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0D,
                0.0D,
                0.0D,
                true)
        );
    }
    public static void stopLocalSounds() {
        stopTrackedSounds();
    }

    private static void stopTrackedSounds() {
        Minecraft minecraft = Minecraft.getInstance();
        for (ResourceLocation soundId : activeSoundIds) {
            minecraft.getSoundManager().stop(soundId, SoundSource.PLAYERS);
        }
        activeSoundIds.clear();
    }

    public static void stop(StopSoundPacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        UUID currentSession = ClientAnimationManager.getSessionId(minecraft.player);
        if (currentSession == null || currentSession.equals(packet.sessionId())) {
            minecraft.getSoundManager().stop(packet.soundId(), SoundSource.PLAYERS);
        }
    }
}

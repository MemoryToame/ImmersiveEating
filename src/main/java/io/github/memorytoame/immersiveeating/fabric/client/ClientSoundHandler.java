package io.github.memorytoame.immersiveeating.fabric.client;

import io.github.memorytoame.immersiveeating.fabric.client.animation.ClientAnimationManager;
import io.github.memorytoame.immersiveeating.fabric.network.packet.StopSoundPacket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public final class ClientSoundHandler {
   private static final Set<ResourceLocation> activeSoundIds = new HashSet<>(16);

   private ClientSoundHandler() {
   }

   public static void playLocal(ResourceLocation soundId) {
      if (soundId != null) {
         Minecraft minecraft = Minecraft.getInstance();
         activeSoundIds.add(soundId);
         minecraft.getSoundManager()
            .play(new SimpleSoundInstance(soundId, SoundSource.PLAYERS, 0.75F, 1.0F, RandomSource.create(), false, 0, Attenuation.NONE, 0.0, 0.0, 0.0, true));
      }
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


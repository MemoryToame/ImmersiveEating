package io.github.memorytoame.immersiveeating.fabric.client.animation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.LocalPlayer;

@Environment(EnvType.CLIENT)
public final class ClientAnimationManager {
   private static final Map<UUID, ClientAnimationManager.AnimationState> PLAYER_ANIMATIONS = new HashMap<>(16);

   private ClientAnimationManager() {
   }

   public static long getOrCreateControllerId(LocalPlayer player) {
      return player == null
         ? Long.MAX_VALUE
         : PLAYER_ANIMATIONS.computeIfAbsent(player.getUUID(), id -> new ClientAnimationManager.AnimationState(controllerIdFor(id), null)).controllerId();
   }

   public static UUID beginSession(LocalPlayer player) {
      UUID playerId = player.getUUID();
      ClientAnimationManager.AnimationState state = PLAYER_ANIMATIONS.computeIfAbsent(
         playerId, id -> new ClientAnimationManager.AnimationState(controllerIdFor(id), null)
      );
      UUID sessionId = UUID.randomUUID();
      PLAYER_ANIMATIONS.put(playerId, new ClientAnimationManager.AnimationState(state.controllerId(), sessionId));
      return sessionId;
   }

   public static UUID getSessionId(LocalPlayer player) {
      if (player == null) {
         return null;
      }

      ClientAnimationManager.AnimationState state = PLAYER_ANIMATIONS.get(player.getUUID());
      return state == null ? null : state.sessionId();
   }

   public static void clearSession(LocalPlayer player) {
      if (player != null) {
         ClientAnimationManager.AnimationState state = PLAYER_ANIMATIONS.get(player.getUUID());
         if (state != null) {
            PLAYER_ANIMATIONS.put(player.getUUID(), new ClientAnimationManager.AnimationState(state.controllerId(), null));
         }
      }
   }

   public static void clearAll() {
      PLAYER_ANIMATIONS.clear();
   }

   private static long controllerIdFor(UUID playerId) {
      long id = playerId.getMostSignificantBits() ^ Long.rotateLeft(playerId.getLeastSignificantBits(), 1);
      id &= Long.MAX_VALUE;
      return id == 0L ? 1L : id;
   }

   private record AnimationState(long controllerId, UUID sessionId) {
   }
}


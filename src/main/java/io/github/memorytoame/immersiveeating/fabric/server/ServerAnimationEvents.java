package io.github.memorytoame.immersiveeating.fabric.server;

import io.github.memorytoame.immersiveeating.fabric.init.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

public final class ServerAnimationEvents {
   private ServerAnimationEvents() {
   }

   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (ServerAnimationManager.get(player) != null && !ServerAnimationManager.matchesHeldStack(player)) {
               stopAndClear(player);
            }
         }
      });
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> stopAndClear(handler.getPlayer()));
   }

   private static void stopAndClear(ServerPlayer player) {
      ServerAnimationManager.AnimationState state = ServerAnimationManager.get(player);
      if (state != null && state.controllerId() != Long.MAX_VALUE) {
         ModItems.EMPTY.stopTriggeredAnim(player, state.controllerId(), "eat", "eat");
      }
      ServerAnimationManager.clear(player);
   }
}


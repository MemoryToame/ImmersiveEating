package io.github.memorytoame.immersiveeating.fabric.client.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

@Environment(EnvType.CLIENT)
public final class ClientReloadEvents {
   private static boolean registered;
   private static boolean reloaded;

   private ClientReloadEvents() {
   }

   public static void register() {
      if (registered) {
         return;
      }
      registered = true;

      ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
         if (reloaded) {
            return;
         }
         reloaded = true;
      });
      ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
         reloaded = false;
         FirstPersonModEvents.onClientLogout();
      });
   }
}

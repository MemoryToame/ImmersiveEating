package io.github.memorytoame.immersiveeating.fabric.client.network;

import io.github.memorytoame.immersiveeating.fabric.client.ClientSoundHandler;
import io.github.memorytoame.immersiveeating.fabric.network.packet.AnimationPacket;
import io.github.memorytoame.immersiveeating.fabric.network.packet.CustomUsingPacket;
import io.github.memorytoame.immersiveeating.fabric.network.packet.FinishUsePacket;
import io.github.memorytoame.immersiveeating.fabric.network.packet.StopAnimationPacket;
import io.github.memorytoame.immersiveeating.fabric.network.packet.StopSoundPacket;
import java.util.UUID;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;

public final class ClientNetwork {
   private static boolean registered;

   private ClientNetwork() {
   }

   public static void register() {
      if (registered) {
         return;
      }
      registered = true;
      ClientPlayNetworking.registerGlobalReceiver(StopSoundPacket.TYPE, (packet, context) ->
         context.client().execute(() -> ClientSoundHandler.stop(packet))
      );
   }

   public static void sendAnimation(UUID sessionId) {
      ClientPlayNetworking.send(new AnimationPacket(sessionId));
   }

   public static void sendFinishUse(InteractionHand hand, UUID sessionId) {
      ClientPlayNetworking.send(new FinishUsePacket(hand, sessionId));
   }

   public static void sendCustomUsing(InteractionHand hand, Item item, int count) {
      ClientPlayNetworking.send(new CustomUsingPacket(hand, item, count));
   }

   public static void sendStopAnimation(UUID sessionId) {
      ClientPlayNetworking.send(new StopAnimationPacket(sessionId));
   }
}

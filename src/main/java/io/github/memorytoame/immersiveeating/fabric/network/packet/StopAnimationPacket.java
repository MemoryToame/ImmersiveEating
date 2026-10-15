package io.github.memorytoame.immersiveeating.fabric.network.packet;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record StopAnimationPacket(UUID sessionId) implements CustomPacketPayload {
   public static final Type<StopAnimationPacket> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath("food", "stop_animation")
   );
   public static final StreamCodec<FriendlyByteBuf, StopAnimationPacket> STREAM_CODEC = new StreamCodec<>() {
      @Override
      public @NotNull StopAnimationPacket decode(FriendlyByteBuf buf) {
         return new StopAnimationPacket(buf.readUUID());
      }

      @Override
      public void encode(FriendlyByteBuf buf, StopAnimationPacket packet) {
         buf.writeUUID(packet.sessionId());
      }
   };

   @Override
   public @NotNull Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}


package io.github.memorytoame.immersiveeating.fabric.network.packet;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record AnimationPacket(UUID sessionId) implements CustomPacketPayload {
   public static final Type<AnimationPacket> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath("food", "animation")
   );
   public static final StreamCodec<FriendlyByteBuf, AnimationPacket> STREAM_CODEC = new StreamCodec<>() {
      @Override
      public @NotNull AnimationPacket decode(FriendlyByteBuf buf) {
         return new AnimationPacket(buf.readUUID());
      }

      @Override
      public void encode(FriendlyByteBuf buf, AnimationPacket packet) {
         buf.writeUUID(packet.sessionId());
      }
   };

   @Override
   public @NotNull Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}


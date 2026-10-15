package io.github.memorytoame.immersiveeating.fabric.network.packet;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record StopSoundPacket(UUID sessionId, ResourceLocation soundId) implements CustomPacketPayload {
   public static final Type<StopSoundPacket> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath("food", "stop_sound")
   );
   public static final StreamCodec<FriendlyByteBuf, StopSoundPacket> STREAM_CODEC = new StreamCodec<>() {
      @Override
      public @NotNull StopSoundPacket decode(FriendlyByteBuf buf) {
         return new StopSoundPacket(buf.readUUID(), buf.readResourceLocation());
      }

      @Override
      public void encode(FriendlyByteBuf buf, StopSoundPacket packet) {
         buf.writeUUID(packet.sessionId());
         buf.writeResourceLocation(packet.soundId());
      }
   };

   @Override
   public @NotNull Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}


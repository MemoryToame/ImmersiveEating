package io.github.memorytoame.immersiveeating.fabric.network.packet;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;

public record FinishUsePacket(InteractionHand hand, UUID sessionId) implements CustomPacketPayload {
   public static final Type<FinishUsePacket> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath("food", "finish_use")
   );
   public static final StreamCodec<FriendlyByteBuf, FinishUsePacket> STREAM_CODEC = new StreamCodec<>() {
      @Override
      public @NotNull FinishUsePacket decode(FriendlyByteBuf buf) {
         return new FinishUsePacket(buf.readEnum(InteractionHand.class), buf.readUUID());
      }

      @Override
      public void encode(FriendlyByteBuf buf, FinishUsePacket packet) {
         buf.writeEnum(packet.hand());
         buf.writeUUID(packet.sessionId());
      }
   };

   @Override
   public @NotNull Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}


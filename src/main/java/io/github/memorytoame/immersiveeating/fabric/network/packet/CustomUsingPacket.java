package io.github.memorytoame.immersiveeating.fabric.network.packet;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record CustomUsingPacket(InteractionHand hand, Item customItem, int count) implements CustomPacketPayload {
   public static final Type<CustomUsingPacket> TYPE = new Type<>(
      ResourceLocation.fromNamespaceAndPath("food", "custom_using")
   );
   public static final StreamCodec<FriendlyByteBuf, CustomUsingPacket> STREAM_CODEC = new StreamCodec<>() {
      @Override
      public @NotNull CustomUsingPacket decode(FriendlyByteBuf buf) {
         InteractionHand hand = buf.readEnum(InteractionHand.class);
         Item item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
         return new CustomUsingPacket(hand, item, buf.readVarInt());
      }

      @Override
      public void encode(FriendlyByteBuf buf, CustomUsingPacket packet) {
         buf.writeEnum(packet.hand());
         buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(packet.customItem()));
         buf.writeVarInt(packet.count());
      }
   };

   @Override
   public @NotNull Type<? extends CustomPacketPayload> type() {
      return TYPE;
   }
}


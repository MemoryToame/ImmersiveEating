package io.github.memorytoame.immersiveeating.neoforge.network.packet;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.unique.UniqueItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CustomUsingPacket(InteractionHand hand, Item customItem, int count) implements CustomPacketPayload {
    public static final Type<CustomUsingPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Food.MODID, "custom_using"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CustomUsingPacket> STREAM_CODEC = StreamCodec.of((buffer, payload) -> {
        buffer.writeEnum(payload.hand);
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(payload.customItem);
        buffer.writeResourceLocation(itemId == null ? BuiltInRegistries.ITEM.getKey(net.minecraft.world.item.Items.AIR) : itemId);
        buffer.writeVarInt(payload.count);
        },
            buffer -> new CustomUsingPacket(buffer.readEnum(InteractionHand.class),
            BuiltInRegistries.ITEM.get(buffer.readResourceLocation()),
            buffer.readVarInt())
    );

    @Override
    public Type<CustomUsingPacket> type() {
        return TYPE;
    }

    public static void handle(CustomUsingPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp)) {
                return;
            }

            if (payload.customItem == null || payload.customItem == net.minecraft.world.item.Items.AIR) {
                return;
            }

            ItemStack stack = new ItemStack(payload.customItem, payload.count);
            if (!stack.has(DataComponents.FOOD) || stack.getUseDuration(sp) <= 0) {
                return;
            }

            stack.finishUsingItem(sp.level(), sp);
        });
    }
}

package io.github.memorytoame.immersiveeating.neoforge.network.packet;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FinishUsePacket(InteractionHand hand) implements CustomPacketPayload {
    public static final Type<FinishUsePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Food.MODID, "finish_use")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FinishUsePacket> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeEnum(payload.hand),
            buffer -> new FinishUsePacket(buffer.readEnum(InteractionHand.class))
    );

    @Override
    public Type<FinishUsePacket> type() {
        return TYPE;
    }

    public static void handle(FinishUsePacket payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        ItemStack stack = player.getItemInHand(payload.hand);
        if (stack.isEmpty() || !stack.has(DataComponents.FOOD) || stack.getUseDuration(player) <= 0) {
            return;
        }

        player.setItemInHand(payload.hand, stack.finishUsingItem(player.level(), player));
    }
}

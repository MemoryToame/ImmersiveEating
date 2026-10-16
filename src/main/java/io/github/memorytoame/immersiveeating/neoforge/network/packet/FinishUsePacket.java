package io.github.memorytoame.immersiveeating.neoforge.network.packet;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.server.ServerAnimationManager;
import io.github.memorytoame.immersiveeating.neoforge.unique.UniqueItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record FinishUsePacket(InteractionHand hand, UUID sessionId) implements CustomPacketPayload {
    public static final Type<FinishUsePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Food.MODID, "finish_use")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FinishUsePacket> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeEnum(payload.hand);
                buffer.writeUUID(payload.sessionId);
            },
            buffer -> new FinishUsePacket(buffer.readEnum(InteractionHand.class), buffer.readUUID())
    );

    @Override
    public Type<FinishUsePacket> type() {
        return TYPE;
    }

    public static void handle(FinishUsePacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp) || payload.sessionId() == null
                    || !ServerAnimationManager.matchesSession(sp, payload.sessionId(), payload.hand)
                    || !ServerAnimationManager.matchesHeldStack(sp)) {
                return;
            }
            ItemStack stack = sp.getItemInHand(payload.hand);
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (UniqueItem.UNIQUE_ITEM_MAP.containsKey(itemId)) {
                stack.shrink(1);
                sp.setItemInHand(payload.hand, stack);
                sp.getInventory().add(new ItemStack(Items.BOWL, 1));
                ServerAnimationManager.clear(sp);
                return;
            }
            if (stack.isEmpty()) {
                return;
            }
            sp.setItemInHand(payload.hand, stack.finishUsingItem(sp.level(), sp));
            ServerAnimationManager.clear(sp);
        });
    }
}

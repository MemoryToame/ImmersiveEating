package io.github.memorytoame.immersiveeating.neoforge.network.packet;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.init.ModItems;
import io.github.memorytoame.immersiveeating.neoforge.server.ServerAnimationManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record StopAnimationPacket(UUID sessionId) implements CustomPacketPayload {
    public static final Type<StopAnimationPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Food.MODID, "stop_animation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StopAnimationPacket> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> buffer.writeUUID(payload.sessionId),
            buffer -> new StopAnimationPacket(buffer.readUUID())
    );

    @Override
    public Type<StopAnimationPacket> type() {
        return TYPE;
    }

    public static void handle(StopAnimationPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp) || payload.sessionId() == null
                    || !ServerAnimationManager.matchesSession(sp, payload.sessionId(), InteractionHand.MAIN_HAND)) {
                return;
            }
            ServerAnimationManager.AnimationState state = ServerAnimationManager.get(sp);
            if (state.controllerId() != Long.MAX_VALUE) {
                ModItems.EMPTY.get().stopTriggeredAnim(sp, state.controllerId(), "eat", "eat");
            }
            ServerAnimationManager.clear(sp);
        });
    }
}

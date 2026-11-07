package io.github.memorytoame.immersiveeating.neoforge.network.packet;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import io.github.memorytoame.immersiveeating.neoforge.utils.AnimationUtils;
import java.util.UUID;

public record AnimationStatusPacket(UUID sessionId, boolean accepted) implements CustomPacketPayload {
    public static final Type<AnimationStatusPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Food.MODID, "animation_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AnimationStatusPacket> STREAM_CODEC = StreamCodec.of((buffer, payload) -> {
        buffer.writeUUID(payload.sessionId);
        buffer.writeBoolean(payload.accepted);
    }, buffer -> new AnimationStatusPacket(buffer.readUUID(), buffer.readBoolean()));

    @Override
    public Type<AnimationStatusPacket> type() {
        return TYPE;
    }

    public static void handle(AnimationStatusPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> AnimationUtils.onAnimationStatus(payload.sessionId(), payload.accepted()));
    }
}

package io.github.memorytoame.immersiveeating.neoforge.network.packet;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.client.ClientSoundHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

    public record StopSoundPacket(UUID sessionId, ResourceLocation soundId) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<StopSoundPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Food.MODID, "stop_sound"));

    public static final StreamCodec<FriendlyByteBuf, StopSoundPacket> STREAM_CODEC = StreamCodec.of((buf, packet) -> {buf.writeUUID(packet.sessionId);buf.writeResourceLocation(packet.soundId);}, buf -> new StopSoundPacket(buf.readUUID(), buf.readResourceLocation()));


    @Override

    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(StopSoundPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> ClientSoundHandler.stop(packet));
    }
}

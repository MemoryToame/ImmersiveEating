package io.github.memorytoame.immersiveeating.neoforge.network.packet;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SoundPacket(ResourceLocation itemId, ResourceLocation soundId) implements CustomPacketPayload {
    public static final Type<SoundPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Food.MODID, "sound")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SoundPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            SoundPacket::itemId,
            ResourceLocation.STREAM_CODEC,
            SoundPacket::soundId,
            SoundPacket::new
    );

    @Override
    public Type<SoundPacket> type() {
        return TYPE;
    }

    public static void handle(SoundPacket payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        ResourceLocation heldId = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem());
        if (!payload.itemId.equals(heldId)) {
            return;
        }

        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvent.createVariableRangeEvent(payload.soundId), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}

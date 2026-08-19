package org.toame.food.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class SoundPacket {
    private final ResourceLocation itemId;
    private final ResourceLocation soundId;

    public SoundPacket(ResourceLocation itemId, ResourceLocation soundId) {
        this.itemId = itemId;
        this.soundId = soundId;
    }

    public static void encode(SoundPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.itemId);
        buffer.writeResourceLocation(packet.soundId);
    }
    public static SoundPacket decode(FriendlyByteBuf buffer) {
        return new SoundPacket(
                buffer.readResourceLocation(),
                buffer.readResourceLocation()
        );
    }
    public static void handle(SoundPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            ResourceLocation heldId = ForgeRegistries.ITEMS.getKey(player.getMainHandItem().getItem());
            if (heldId == null || !heldId.equals(packet.itemId)) {
                return;
            }
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvent.createVariableRangeEvent(packet.soundId), SoundSource.PLAYERS, 1.0F, 1.0F);
        });
        context.setPacketHandled(true);
    }
}

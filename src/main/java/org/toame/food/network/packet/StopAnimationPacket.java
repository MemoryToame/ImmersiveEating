package org.toame.food.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.toame.food.network.ServerAnimationManager;
import java.util.UUID;
import java.util.function.Supplier;

public record StopAnimationPacket(UUID sessionId) {
    public static void encode(StopAnimationPacket packet, FriendlyByteBuf buffer) { buffer.writeUUID(packet.sessionId); }
    public static StopAnimationPacket decode(FriendlyByteBuf buffer) { return new StopAnimationPacket(buffer.readUUID()); }
    public static void handle(StopAnimationPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            ServerAnimationManager.AnimationState state = ServerAnimationManager.get(player);
            if (player == null || state == null || !state.sessionId().equals(packet.sessionId)) return;
            ServerAnimationManager.clear(player);
        });
        context.setPacketHandled(true);
    }
}


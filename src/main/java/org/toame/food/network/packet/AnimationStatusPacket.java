package org.toame.food.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import org.toame.food.utils.AnimationUtils;

import java.util.UUID;
import java.util.function.Supplier;

public record AnimationStatusPacket(UUID sessionId, boolean accepted) {
    public static void encode(AnimationStatusPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.sessionId);
        buffer.writeBoolean(packet.accepted);
    }

    public static AnimationStatusPacket decode(FriendlyByteBuf buffer) {
        return new AnimationStatusPacket(buffer.readUUID(), buffer.readBoolean());
    }

    public static void handle(AnimationStatusPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> AnimationUtils.onAnimationStatus(packet.sessionId, packet.accepted));
        context.setPacketHandled(true);
    }
}

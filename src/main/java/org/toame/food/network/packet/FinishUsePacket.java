package org.toame.food.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FinishUsePacket {
    private final InteractionHand hand;

    public FinishUsePacket(InteractionHand hand) {
        this.hand = hand;
    }
    public static void encode(FinishUsePacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.hand);
    }

    public static FinishUsePacket decode(FriendlyByteBuf buffer) {
        return new FinishUsePacket(buffer.readEnum(InteractionHand.class));
    }

    public static void handle(FinishUsePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer sp = context.getSender();

            if (sp == null) {
                return;
            }

            ItemStack stack = sp.getItemInHand(packet.hand);

            if (stack.isEmpty() || !stack.isEdible() || stack.getUseDuration() <= 0) {
                return;
            }
            ItemStack result = stack.finishUsingItem(sp.level(), sp);
            sp.setItemInHand(packet.hand, result);
        });

        context.setPacketHandled(true);
    }
}

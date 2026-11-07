package org.toame.food.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.network.Network;
import net.minecraftforge.network.PacketDistributor;
import org.toame.food.init.ModItems;
import org.toame.food.network.ServerAnimationManager;

import java.util.UUID;
import java.util.function.Supplier;

public record AnimationPacket(UUID sessionId) {
    public static void encode(AnimationPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.sessionId);
    }

    public static AnimationPacket decode(FriendlyByteBuf buffer) {
        return new AnimationPacket(buffer.readUUID());
    }

    public static void handle(AnimationPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sp = context.getSender();
            if (sp == null || packet.sessionId() == null) {
                return;
            }
            ItemStack stack = sp.getMainHandItem();
            boolean accepted = !stack.isEmpty() && !stack.is(ModItems.EMPTY.get())
                    && FoodDefinitionManager.init_ItemList.contains(stack.getItem());
            if (accepted) {
                ServerAnimationManager.begin(sp, packet.sessionId(), InteractionHand.MAIN_HAND, stack);
            }
            Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp),
                    new AnimationStatusPacket(packet.sessionId(), accepted));
        });
        context.setPacketHandled(true);
    }
}

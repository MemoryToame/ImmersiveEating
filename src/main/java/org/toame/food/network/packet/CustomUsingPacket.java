package org.toame.food.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class CustomUsingPacket {
    private final InteractionHand hand;
    private final Item customItem;
    private final Integer count;

    public CustomUsingPacket(InteractionHand hand, Item customItem,Integer count) {
        this.hand = hand;
        this.customItem=customItem;
        this.count=count;
    }
    public static void encode(CustomUsingPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.hand);
        buffer.writeResourceLocation(ForgeRegistries.ITEMS.getKey(packet.customItem));
        buffer.writeInt(packet.count);
    }

    public static CustomUsingPacket decode(FriendlyByteBuf buffer) {
        InteractionHand hand = buffer.readEnum(InteractionHand.class);
        ResourceLocation itemId = buffer.readResourceLocation();
        Item customItem = ForgeRegistries.ITEMS.getValue(itemId);
        Integer count = buffer.readInt();
        return new CustomUsingPacket(hand, customItem,count);
    }

    public static void handle(CustomUsingPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            ServerPlayer sp = context.getSender();

            if (sp == null) {
                return;
            }
            if (packet.customItem == null || packet.customItem == net.minecraft.world.item.Items.AIR) {
                return;
            }
            ItemStack stack = new ItemStack(packet.customItem, packet.count);
            if (!stack.isEdible() || stack.getUseDuration() <= 0) {
                return;
            }

            stack.finishUsingItem(sp.level(), sp);
        });

        context.setPacketHandled(true);
    }
}

package org.toame.food.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.toame.food.init.ModItems;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.function.Supplier;

public class AnimationPacket {
    public AnimationPacket() {
    }
    public static void encode(AnimationPacket packet, FriendlyByteBuf buffer) {
    }
    public static AnimationPacket decode(FriendlyByteBuf buffer) {
        return new AnimationPacket();
    }
    public static void handle(AnimationPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }
            ItemStack stack = player.getMainHandItem();

                if (stack.isEmpty() || stack.is(ModItems.EMPTY.get())) {
                return;
            }
            long id = GeoItem.getOrAssignId(ModItems.EMPTY.get().getRenderStack(), player.serverLevel());

            ModItems.EMPTY.get().triggerAnim(player, id, "eat", "eat");
        });

        context.setPacketHandled(true);
    }
}

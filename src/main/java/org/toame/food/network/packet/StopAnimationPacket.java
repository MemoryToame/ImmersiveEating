package org.toame.food.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.toame.food.init.ModItems;
import software.bernie.geckolib.animatable.GeoItem;

import java.util.function.Supplier;

public class StopAnimationPacket {
    public StopAnimationPacket() {
    }

    public static void encode(StopAnimationPacket packet, FriendlyByteBuf buffer) {
    }

    public static StopAnimationPacket decode(FriendlyByteBuf buffer) {
        return new StopAnimationPacket();
    }

    public static void handle(StopAnimationPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer sp = context.getSender();
            if (sp == null) {
                return;
            }

            ItemStack stack = sp.getMainHandItem();
            if (stack.isEmpty() || stack.is(ModItems.EMPTY.get())) {
                return;
            }

            long id = GeoItem.getId(stack);
            if (id == Long.MAX_VALUE) {
                return;
            }

            ModItems.EMPTY.get().stopTriggeredAnim(sp, id, "eat", "eat");
        });
        context.setPacketHandled(true);
    }
}

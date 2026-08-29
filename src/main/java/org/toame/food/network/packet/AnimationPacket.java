package org.toame.food.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.toame.food.init.ModItems;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;

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
            ServerPlayer sp = context.getSender();
            if (sp == null) {
                return;
            }
            ItemStack stack = sp.getMainHandItem();

            if (stack.isEmpty() || stack.is(ModItems.EMPTY.get())) {
                return;
            }
            long id = GeoItem.getOrAssignId(stack, sp.serverLevel());
            sp.setItemInHand(InteractionHand.MAIN_HAND, stack);

            AnimationController<GeoAnimatable> controller = ModItems.EMPTY.get().getAnimatableInstanceCache().getManagerForId(id).getAnimationControllers().get("eat");
            if (controller != null && controller.isPlayingTriggeredAnimation()) {
                return;
            }
            ModItems.EMPTY.get().triggerAnim(sp, id, "eat", "eat");
        });

        context.setPacketHandled(true);
    }
}

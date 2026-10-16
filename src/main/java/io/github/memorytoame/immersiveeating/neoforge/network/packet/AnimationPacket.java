package io.github.memorytoame.immersiveeating.neoforge.network.packet;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.init.ModItems;
import io.github.memorytoame.immersiveeating.neoforge.server.ServerAnimationManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animation.AnimationController;
import java.util.UUID;

public record AnimationPacket(UUID sessionId) implements CustomPacketPayload {
    public static final Type<AnimationPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Food.MODID, "animation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AnimationPacket> STREAM_CODEC = StreamCodec.of((buffer, payload) -> buffer.writeUUID(payload.sessionId), buffer -> new AnimationPacket(buffer.readUUID()));

    @Override
    public Type<AnimationPacket> type() {
        return TYPE;
    }

    public static void handle(AnimationPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer sp) || payload.sessionId() == null) {
                return;
            }
            ItemStack stack = sp.getMainHandItem();
            if (stack.isEmpty() || stack.is(ModItems.EMPTY.get()) || !FoodDefinitionManager.init_ItemList.contains(stack.getItem())) {
                return;
            }
            long id = GeoItem.getOrAssignId(stack, sp.serverLevel());
            sp.setItemInHand(InteractionHand.MAIN_HAND, stack);
            AnimationController<GeoAnimatable> controller = ModItems.EMPTY.get().getAnimatableInstanceCache()
                    .getManagerForId(id).getAnimationControllers().get("eat");
            if (controller != null && controller.isPlayingTriggeredAnimation()) {
                return;
            }
            if (!ServerAnimationManager.begin(sp, payload.sessionId(), InteractionHand.MAIN_HAND, stack, id)) {
                return;
            }
            ModItems.EMPTY.get().triggerAnim(sp, id, "eat", "eat");
        });
    }
}

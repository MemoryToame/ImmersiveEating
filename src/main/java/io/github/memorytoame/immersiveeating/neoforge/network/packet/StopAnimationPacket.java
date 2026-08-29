package io.github.memorytoame.immersiveeating.neoforge.network.packet;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.init.ModItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import software.bernie.geckolib.animatable.GeoItem;

public record StopAnimationPacket() implements CustomPacketPayload {
    public static final Type<StopAnimationPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Food.MODID, "stop_animation"));
    public static final StreamCodec<RegistryFriendlyByteBuf, StopAnimationPacket> STREAM_CODEC = StreamCodec.unit(new StopAnimationPacket());

    @Override
    public Type<StopAnimationPacket> type() {
        return TYPE;
    }

    public static void handle(StopAnimationPacket payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer sp)) {
            return;
        }
        ItemStack stack = sp.getMainHandItem();
        if (stack.isEmpty() || stack.is(ModItems.EMPTY.get())) {
            return;
        }
        long id = GeoItem.getId(stack);
        if (id != Long.MAX_VALUE) {
            ModItems.EMPTY.get().stopTriggeredAnim(sp, id, "eat", "eat");
        }
    }
}

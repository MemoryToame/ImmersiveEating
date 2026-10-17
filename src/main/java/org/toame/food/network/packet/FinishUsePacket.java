package org.toame.food.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.toame.food.compat.SpiceOfLifeValheim;
import org.toame.food.compat.SpiceofLifeOnion;
import org.toame.food.network.ServerAnimationManager;
import org.toame.food.unique.UniqueItem;
import java.util.UUID;
import java.util.function.Supplier;

public record FinishUsePacket(UUID sessionId, InteractionHand hand) {
    public static void encode(FinishUsePacket packet, FriendlyByteBuf buffer) { buffer.writeUUID(packet.sessionId); buffer.writeEnum(packet.hand); }
    public static FinishUsePacket decode(FriendlyByteBuf buffer) { return new FinishUsePacket(buffer.readUUID(), buffer.readEnum(InteractionHand.class)); }
    public static void handle(FinishUsePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || packet.hand == null || !ServerAnimationManager.matchesSession(player, packet.sessionId, packet.hand)) return;
            try {
                ItemStack stack = player.getItemInHand(packet.hand);
                if (UniqueItem.UNIQUE_ITEM_MAP.get(ForgeRegistries.ITEMS.getKey(stack.getItem())) != null) {
                    stack.shrink(1);
                    player.setItemInHand(packet.hand, stack);
                    player.getInventory().add(new ItemStack(Items.BOWL));
                    player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (stack.isEmpty() || !stack.isEdible()) {
                    if (!new ResourceLocation("kaleidoscope_cookery", "sakura_fubuki").equals(id) && !new ResourceLocation("kaleidoscope_cookery", "flower_tea").equals(id)) return;
                }
                if (!SpiceOfLifeValheim.enable(player,stack)){
                    SpiceofLifeOnion.enable(player,stack);
                    player.setItemInHand(packet.hand, stack.finishUsingItem(player.level(), player));
                }
            } finally {
                ServerAnimationManager.clear(player);
            }
        });
        context.setPacketHandled(true);
    }
}


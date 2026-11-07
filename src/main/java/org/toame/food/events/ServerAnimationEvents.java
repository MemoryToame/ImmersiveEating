package org.toame.food.events;

import org.toame.food.Food;
import org.toame.food.network.Network;
import org.toame.food.network.packet.AnimationStatusPacket;
import net.minecraftforge.network.PacketDistributor;
import org.toame.food.network.ServerAnimationManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;

@Mod.EventBusSubscriber(modid = Food.MODID)
public final class ServerAnimationEvents {

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerAnimationManager.clear(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END || !(event.player instanceof ServerPlayer sp) || sp.level().isClientSide) {
            return;
        }
        ServerAnimationManager.AnimationState state = ServerAnimationManager.get(sp);
        if (state == null || ServerAnimationManager.matchesHeldStack(sp)) {
            return;
        }

        ServerAnimationManager.clear(sp);
        Network.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new AnimationStatusPacket(state.sessionId(), false));
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerAnimationManager.clearAll();
    }
}

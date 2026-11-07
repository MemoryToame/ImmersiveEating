package io.github.memorytoame.immersiveeating.neoforge.server;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.AnimationStatusPacket;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Food.MODID)
public final class ServerAnimationEvents {
    private ServerAnimationEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerAnimationManager.clear(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer sp) || sp.level().isClientSide) {
            return;
        }

        ServerAnimationManager.AnimationState state = ServerAnimationManager.get(sp);
        if (state == null || ServerAnimationManager.matchesHeldStack(sp)) {
            return;
        }

        ServerAnimationManager.clear(sp);
        PacketDistributor.sendToPlayer(sp, new AnimationStatusPacket(state.sessionId(), false));
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        ServerAnimationManager.clearAll();
    }
}

package io.github.memorytoame.immersiveeating.neoforge.events;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
public class ClientReloadEvents {

    private static boolean reloaded = false;

    @SubscribeEvent
    public static void onClientPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        if (reloaded) {
            return;
        }
        reloaded = true;
    }
}
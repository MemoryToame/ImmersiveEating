package org.toame.food.events;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.toame.food.Food;

@Mod.EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
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
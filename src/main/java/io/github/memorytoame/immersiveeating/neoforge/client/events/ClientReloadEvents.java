package io.github.memorytoame.immersiveeating.neoforge.client.events;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
public class ClientReloadEvents {

    private static boolean reloaded = false;

    @SubscribeEvent
    public static void onClientPlayerLoggingIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getPlayer() != mc.player) {
            return;
        }
        if (reloaded) {
            return;
        }
        reloaded = true;
    }
}

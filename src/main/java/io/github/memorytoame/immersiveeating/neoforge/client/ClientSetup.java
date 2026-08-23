package io.github.memorytoame.immersiveeating.neoforge.client;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.additions.Empty;
import io.github.memorytoame.immersiveeating.neoforge.additions.definiton.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.events.InputEvents;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import software.bernie.geckolib.GeckoLibClient;

@EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        GeckoLibClient.init();
        Empty.setClientAnimationHooks(stack -> InputEvents.playCustomUseReequipAnimation(Minecraft.getInstance(), stack), CameraShake::trigger);
    }
    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new FoodDefinitionManager());
    }
}

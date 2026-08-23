package org.toame.food.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.toame.food.Food;
import org.toame.food.additions.Empty;
import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.events.InputEvents;

@Mod.EventBusSubscriber(modid = Food.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        Empty.setClientAnimationHooks(stack -> InputEvents.playCustomUseReequipAnimation(Minecraft.getInstance(), stack), CameraShake::trigger);
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new FoodDefinitionManager());
    }
}

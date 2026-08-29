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
import org.toame.food.utils.AnimationUtils;

@Mod.EventBusSubscriber(modid = Food.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void setupClient(FMLClientSetupEvent event) {
        Empty.setClientHooks(
                stack -> AnimationUtils.playCustomUseReequipAnimation(Minecraft.getInstance(), stack),
                CameraShake::trigger,
                () -> {
                    Minecraft mc = Minecraft.getInstance();
                    return mc.player == null ? net.minecraft.world.item.ItemStack.EMPTY : mc.player.getMainHandItem();
                },
                () -> AnimationUtils.temp = null,
                () -> AnimationUtils.lockedHotbarSlot = -1,
                ()-> Minecraft.getInstance().player.getMainHandItem().getItem()
        );
        Empty.setMainHandItemChecker(item -> {
            Minecraft mc = Minecraft.getInstance();
            return mc.player != null && mc.player.getMainHandItem().is(item);
        });

    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new FoodDefinitionManager());
    }
}

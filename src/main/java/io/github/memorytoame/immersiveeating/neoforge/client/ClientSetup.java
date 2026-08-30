package io.github.memorytoame.immersiveeating.neoforge.client;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.additions.Empty;
import io.github.memorytoame.immersiveeating.neoforge.additions.definiton.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.utils.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

@EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
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
    }
    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new FoodDefinitionManager());
    }
}

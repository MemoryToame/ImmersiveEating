package io.github.memorytoame.immersiveeating.neoforge.client;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.common.content.item.Empty;
import io.github.memorytoame.immersiveeating.neoforge.common.content.definition.FoodDefinitionManager;
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
                () -> {
                    Minecraft mc = Minecraft.getInstance();
                    return mc.player == null ? net.minecraft.world.item.ItemStack.EMPTY : mc.player.getMainHandItem();
                },
                () -> AnimationUtils.temp = null,
                AnimationUtils::finishAnimationSession,
                ()-> Minecraft.getInstance().player == null ? net.minecraft.world.item.Items.AIR : Minecraft.getInstance().player.getMainHandItem().getItem(),
                () -> AnimationUtils.getAnimationSessionId(Minecraft.getInstance()),
                () -> {
                    AnimationUtils.refreshAnimationControllerId(Minecraft.getInstance());
                    return AnimationUtils.getAnimationControllerId(Minecraft.getInstance());
                }
        );
        Empty.setClientSoundHooks(
                io.github.memorytoame.immersiveeating.neoforge.client.ClientSoundHandler::playLocal,
                io.github.memorytoame.immersiveeating.neoforge.client.ClientSoundHandler::stopLocalSounds
        );
    }
    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new FoodDefinitionManager());
    }
}

package org.toame.food.client;

import org.toame.food.Food;
import org.toame.food.additions.Empty;
import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.compat.RealCamera;
import org.toame.food.utils.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;

import java.lang.reflect.InvocationTargetException;

@Mod.EventBusSubscriber(modid = Food.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
                AnimationUtils::getAnimationSessionId,
                () -> {
                    AnimationUtils.refreshAnimationControllerId(Minecraft.getInstance());
                    return AnimationUtils.getAnimationControllerId();
                }
        );
        Empty.setClientSoundHooks(
                ClientSoundHandler::playLocal,
                ClientSoundHandler::stopLocalSounds
        );
        try {
            RealCamera.disableRealCamera();
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new FoodDefinitionManager());
    }

}

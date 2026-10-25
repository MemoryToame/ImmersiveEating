package io.github.memorytoame.immersiveeating.neoforge.compat;

import com.mojang.logging.LogUtils;
import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.client.animation.ClientAnimationManager;
import io.github.memorytoame.immersiveeating.neoforge.mixin.Accessor.ItemInHandRendererAccessor;
import io.github.memorytoame.immersiveeating.neoforge.utils.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.Logger;

import java.lang.reflect.Method;

@EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
public final class FirstPerson {

    private static final float VANILLA_PITCH_MAX = 60.0F;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean lookupDone;
    //强制使用原版第一人称
    private static boolean forcedVanilla;
    private static boolean previousEnabled;
    private static Method isEnabledMethod;
    private static Method setEnabledMethod;

    private FirstPerson() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) {
            restorePreviousState();
            return;
        }
        if (!findApi()) {
            return;
        }
        if (AnimationUtils.isAnimationLocked()){
            forceVanillaFirstPerson(mc);
            return;
        }
        if (mc.player.getXRot() <= VANILLA_PITCH_MAX&& FoodDefinitionManager.init_ItemList.contains(mc.player.getMainHandItem().getItem())) {
            forceVanillaFirstPerson(mc);
        } else {
            restorePreviousState();
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        Minecraft mc = Minecraft.getInstance();
        if (event.getPlayer() != null && mc.player != null && event.getPlayer() != mc.player) {
            return;
        }
        restorePreviousState();
        ClientAnimationManager.clearAll();
        AnimationUtils.clearAnimationState();
    }

    private static boolean findApi() {
        if (lookupDone) {
            return isEnabledMethod != null && setEnabledMethod != null;
        }
        lookupDone = true;
        if (!ModList.get().isLoaded("firstperson")) {
            return false;
        }
        try {
            Class<?> api = Class.forName("dev.tr7zw.firstperson.api.FirstPersonAPI", false, FirstPerson.class.getClassLoader());
            isEnabledMethod = api.getMethod("isEnabled");
            setEnabledMethod = api.getMethod("setEnabled", boolean.class);
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.warn("FirstPersonMod doesn't exist.");
            return false;
        }
    }
    private static boolean isFirstPersonModlEnabled() {
        try {
            return (boolean) isEnabledMethod.invoke(null);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.warn("Could not read FirstPersonMod state.", exception);
            return false;
        }
    }

    private static void setFirstPersonModEnabled(boolean enabled) {
        try {
            setEnabledMethod.invoke(null, enabled);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.warn("Could not change FirstPersonMod state.", exception);
        }
    }

    private static void forceVanillaFirstPerson(Minecraft minecraft) {
        if (!forcedVanilla) {
            previousEnabled = isFirstPersonModlEnabled();
            forcedVanilla = true;
            if (previousEnabled) {
                playVanillaReequipAnimation(minecraft);
            }
        }
        setFirstPersonModEnabled(false);
    }

    private static void playVanillaReequipAnimation(Minecraft minecraft) {
        ItemStack stack = minecraft.player.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }
        ItemInHandRendererAccessor itemInHandRenderer = (ItemInHandRendererAccessor) minecraft.gameRenderer.itemInHandRenderer;
        ItemStack fakeStack = stack.copy();
        fakeStack.setCount(stack.getCount() + 1);
        itemInHandRenderer.setMainHandItem(fakeStack);
    }

    private static void restorePreviousState() {
        if (!forcedVanilla || !findApi()) {
            forcedVanilla = false;
            return;
        }
        setFirstPersonModEnabled(previousEnabled);
        forcedVanilla = false;
    }
}

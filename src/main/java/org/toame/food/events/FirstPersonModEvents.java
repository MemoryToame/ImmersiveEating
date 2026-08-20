package org.toame.food.events;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.toame.food.Food;
import org.toame.food.additions.CustomRenderer;
import org.toame.food.client.HeldItemMotion;
import org.toame.food.mixin.Accessor.ItemInHandRendererAccessor;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
public final class FirstPersonModEvents {

    private static final float VANILLA_PITCH_MAX = 60.0F;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean lookupDone;
    private static boolean forcedVanilla;
    private static boolean previousEnabled;
    private static Method isEnabledMethod;
    private static Method setEnabledMethod;

    private FirstPersonModEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        HeldItemMotion.tickInertia();
        HeldItemMotion.tickJump();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) {
            restorePreviousState();
            return;
        }
        if (!findApi()) {
            return;
        }
        if (InputEvents.isAnimationLocked()){
            setFirstPersonModEnabled(false);
            return;
        }
        if (mc.player.getXRot() <= VANILLA_PITCH_MAX&& CustomRenderer.init_ItemList.contains(mc.player.getMainHandItem().getItem())) {
            if (!forcedVanilla) {
                previousEnabled = isFirstPersonModlEnabled();
                forcedVanilla = true;
                if (previousEnabled) {
                    playVanillaReequipAnimation(mc);
                }
            }
            setFirstPersonModEnabled(false);
        } else {
            restorePreviousState();
        }
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        restorePreviousState();
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
            Class<?> api = Class.forName("dev.tr7zw.firstperson.api.FirstPersonAPI", false, FirstPersonModEvents.class.getClassLoader());
            isEnabledMethod = api.getMethod("isEnabled");
            setEnabledMethod = api.getMethod("setEnabled", boolean.class);
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.warn("FirstPersonMod API was not found.", exception);
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

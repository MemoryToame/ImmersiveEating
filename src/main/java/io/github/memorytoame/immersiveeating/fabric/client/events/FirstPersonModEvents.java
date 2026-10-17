package io.github.memorytoame.immersiveeating.fabric.client.events;

import io.github.memorytoame.immersiveeating.fabric.KaleidoscopeImmersiveEating;
import io.github.memorytoame.immersiveeating.fabric.client.animation.ClientAnimationManager;
import io.github.memorytoame.immersiveeating.fabric.client.mixin.accessor.ItemInHandRendererAccessor;
import io.github.memorytoame.immersiveeating.fabric.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.fabric.utils.AnimationUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

@Environment(EnvType.CLIENT)
public final class FirstPersonModEvents {

   private static final float VANILLA_PITCH_MAX = 60.0F;
   private static boolean lookupDone;
   //强制使用原版第一人称
   private static boolean forcedVanilla;
   private static boolean previousEnabled;
   private static Method isEnabledMethod;
   private static Method setEnabledMethod;
   private static boolean registered;

   private FirstPersonModEvents() {
   }

   public static void register() {
      if (registered) {
         return;
      }
      registered = true;
      ClientTickEvents.END_CLIENT_TICK.register(mc -> {
         if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) {
            restorePreviousState();
            return;
         }
         if (!findApi()) {
            return;
         }
         if (AnimationUtils.isAnimationLocked()) {
            forceVanillaFirstPerson(mc);
            return;
         }
         if (mc.player.getXRot() <= VANILLA_PITCH_MAX
            && FoodDefinitionManager.init_ItemList.contains(mc.player.getMainHandItem().getItem())) {
            forceVanillaFirstPerson(mc);
         } else {
            restorePreviousState();
         }
      });
   }

   /** 兼容层 */
   public static void onClientTick() {
      register();
   }

   public static void onClientLogout() {
      restorePreviousState();
      ClientAnimationManager.clearAll();
      AnimationUtils.clearAnimationState();
   }

   private static boolean findApi() {
      if (lookupDone) {
         return isEnabledMethod != null && setEnabledMethod != null;
      }
      lookupDone = true;
      if (!FabricLoader.getInstance().isModLoaded("firstperson")) {
         return false;
      }
      try {
         Class<?> api = Class.forName(
            "dev.tr7zw.firstperson.api.FirstPersonAPI",
            false,
            FirstPersonModEvents.class.getClassLoader()
         );
         isEnabledMethod = api.getMethod("isEnabled");
         setEnabledMethod = api.getMethod("setEnabled", boolean.class);
         return true;
      } catch (ReflectiveOperationException | RuntimeException exception) {
         KaleidoscopeImmersiveEating.LOGGER.warn("FirstPersonMod doesn't exist.", exception);
         return false;
      }
   }
   private static boolean isFirstPersonModEnabled() {
      try {
         return (boolean) isEnabledMethod.invoke(null);
      } catch (ReflectiveOperationException | RuntimeException exception) {
         KaleidoscopeImmersiveEating.LOGGER.warn("Could not read FirstPersonMod state.", exception);
         return false;
      }
   }

   private static void setFirstPersonModEnabled(boolean enabled) {
      try {
         setEnabledMethod.invoke(null, enabled);
      } catch (ReflectiveOperationException | RuntimeException exception) {
         KaleidoscopeImmersiveEating.LOGGER.warn("Could not change FirstPersonMod state.", exception);
      }
   }

   private static void forceVanillaFirstPerson(Minecraft minecraft) {
      if (!forcedVanilla) {
         previousEnabled = isFirstPersonModEnabled();
         forcedVanilla = true;
         if (previousEnabled) {
            playVanillaReequipAnimation(minecraft);
         }
      }
      setFirstPersonModEnabled(false);
   }

   private static void playVanillaReequipAnimation(Minecraft minecraft) {
      if (minecraft.player == null) {
         return;
      }
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

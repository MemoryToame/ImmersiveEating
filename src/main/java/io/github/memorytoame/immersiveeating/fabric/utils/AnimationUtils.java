package io.github.memorytoame.immersiveeating.fabric.utils;

import io.github.memorytoame.immersiveeating.fabric.client.ClientSoundHandler;
import io.github.memorytoame.immersiveeating.fabric.client.animation.AnimationResourceChecker;
import io.github.memorytoame.immersiveeating.fabric.client.animation.CameraRotationComposer;
import io.github.memorytoame.immersiveeating.fabric.client.animation.ClientAnimationManager;
import io.github.memorytoame.immersiveeating.fabric.client.network.ClientNetwork;
import io.github.memorytoame.immersiveeating.fabric.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.fabric.common.content.item.Empty;
import io.github.memorytoame.immersiveeating.fabric.client.mixin.accessor.ItemInHandRendererAccessor;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;

public class AnimationUtils {
   private static UUID animationSessionId;
   private static long animationControllerId = Long.MAX_VALUE;
   public static ItemStack temp;
   public static int lockedHotbarSlot = -1;
   public static boolean wasUsingCustomItem;
   public static boolean customUseReequipHandled;
   public static ItemStack customUseStack = ItemStack.EMPTY;
   public static boolean wasEatAnimationPlaying;
   private static boolean animationRequestPending;
   private static int animationRequestTicks;

   public static boolean isAnimationLocked() {
      return temp != null && lockedHotbarSlot >= 0;
   }

   public static boolean isAnimationRequestPending() {
      return animationRequestPending;
   }

   public static void beginAnimationSession(Minecraft mc) {
      animationSessionId = mc.player == null ? UUID.randomUUID() : ClientAnimationManager.beginSession(mc.player);
      animationControllerId = resolveControllerId(mc);
      animationRequestPending = true;
      animationRequestTicks = 0;
      activateAnimationLock(mc);
   }

   public static UUID getAnimationSessionId(Minecraft mc) {
      return animationSessionId;
   }

   public static long getAnimationControllerId(Minecraft mc) {
      return animationControllerId;
   }

   public static void activateAnimationLock(Minecraft mc) {
      if (mc.player != null && !isAnimationLocked()) {
         lockedHotbarSlot = mc.player.getInventory().selected;
         temp = mc.player.getMainHandItem().copy();
      }
   }

   public static void clearAnimationState() {
      stopAnimationControl();
      animationSessionId = null;
      animationControllerId = Long.MAX_VALUE;
      animationRequestPending = false;
      animationRequestTicks = 0;
   }

   public static void checkEatAnimationState() {
      refreshAnimationControllerId(Minecraft.getInstance());
      if (animationRequestPending && ++animationRequestTicks > 40) {
         stopAnimationControl();
      } else {
         boolean animationPlaying = Empty.isEatAnimationPlaying();
         if (animationPlaying) {
            wasEatAnimationPlaying = true;
            animationRequestPending = false;
         } else if (wasEatAnimationPlaying && isAnimationLocked()) {
            stopAnimationControl();
         }
      }
   }

   public static void stopAnimationControl() {
      if (isAnimationLocked() && animationSessionId != null) {
         ClientNetwork.sendStopAnimation(animationSessionId);
      }

      Empty.stopEatAnimation(animationControllerId);
      ClientSoundHandler.stopLocalSounds();
      if (Minecraft.getInstance().player != null) {
         ClientAnimationManager.clearSession(Minecraft.getInstance().player);
      }

      temp = null;
      lockedHotbarSlot = -1;
      wasEatAnimationPlaying = false;
      animationRequestPending = false;
      animationRequestTicks = 0;
      animationControllerId = Long.MAX_VALUE;
      CameraRotationComposer.clearBlockbenchRotation();
   }

   public static void finishAnimationSession() {
      ClientSoundHandler.stopLocalSounds();
      if (Minecraft.getInstance().player != null) {
         ClientAnimationManager.clearSession(Minecraft.getInstance().player);
      }

      temp = null;
      lockedHotbarSlot = -1;
      wasEatAnimationPlaying = false;
      animationRequestPending = false;
      animationRequestTicks = 0;
      animationSessionId = null;
      animationControllerId = Long.MAX_VALUE;
      CameraRotationComposer.clearBlockbenchRotation();
   }

   public static void refreshAnimationControllerId(Minecraft mc) {
      long id = resolveControllerId(mc);
      if (id != Long.MAX_VALUE) {
         animationControllerId = id;
      }
   }

   private static long resolveControllerId(Minecraft mc) {
      if (mc != null && mc.player != null) {
         long id = GeoItem.getId(mc.player.getMainHandItem());
         return id == Long.MAX_VALUE ? ClientAnimationManager.getOrCreateControllerId(mc.player) : id;
      } else {
         return Long.MAX_VALUE;
      }
   }

   public static void updateCustomUseState(Minecraft mc) {
      if (mc.player == null) {
         resetCustomUseState();
      } else {
         boolean usingCustomItem = mc.player.isUsingItem()
            && mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND
            && FoodDefinitionManager.init_ItemList.contains(mc.player.getUseItem().getItem());
         if (usingCustomItem) {
            if (!wasUsingCustomItem) {
               customUseStack = mc.player.getUseItem().copy();
               customUseReequipHandled = false;
            }

            wasUsingCustomItem = true;
         } else if (wasUsingCustomItem) {
            if (!customUseReequipHandled) {
               playCustomUseReequipAnimation(mc, customUseStack);
            }

            resetCustomUseState();
         }
      }
   }

   public static void cancelIfHeldItemChanged(Minecraft mc) {
      if (isAnimationLocked() && mc.player != null && temp != null) {
         if (!ItemStack.matches(temp, mc.player.getMainHandItem())) {
            stopAnimationControl();
         }
      }
   }

   public static void playCustomUseReequipAnimation(Minecraft mc, ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         if (AnimationResourceChecker.hasAnimation(stack, "switchin")) {
            Empty.triggerClientAnimation(stack, "switchin");
         }

         ItemInHandRendererAccessor renderer = (ItemInHandRendererAccessor)mc.gameRenderer.itemInHandRenderer;
         ItemStack fakeStack = stack.copy();
         fakeStack.setCount(stack.getCount() + 1);
         renderer.setMainHandItem(fakeStack);
         customUseReequipHandled = true;
      }
   }

   private static void resetCustomUseState() {
      wasUsingCustomItem = false;
      customUseReequipHandled = false;
      customUseStack = ItemStack.EMPTY;
   }
}

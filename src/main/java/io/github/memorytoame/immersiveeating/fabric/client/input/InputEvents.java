package io.github.memorytoame.immersiveeating.fabric.client.input;

import io.github.memorytoame.immersiveeating.fabric.Food;
import io.github.memorytoame.immersiveeating.fabric.KaleidoscopeImmersiveEating;
import io.github.memorytoame.immersiveeating.fabric.client.events.GameplayEvents;
import io.github.memorytoame.immersiveeating.fabric.common.config.Config;
import io.github.memorytoame.immersiveeating.fabric.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.fabric.common.content.item.Empty;
import io.github.memorytoame.immersiveeating.fabric.client.network.ClientNetwork;
import io.github.memorytoame.immersiveeating.fabric.utils.AnimationUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class InputEvents {
   public static float debugX = -1.5F;
   public static float debugY = 0.05F;
   public static float debugZ = -0.3F;
   public static float debugRo = 0.0F;
   private static boolean rightClickHandled;

   private InputEvents() {
   }

   public static void register() {
      ClientTickEvents.START_CLIENT_TICK.register(InputEvents::onClientTickStart);
      ClientTickEvents.END_CLIENT_TICK.register(InputEvents::onClientTick);
   }


   public static boolean handleRightClick(Minecraft mc) {
      if (mc == null || !mc.options.keyUse.isDown()) {
         rightClickHandled = false;
      }

      if (mc == null || mc.player == null || !Config.ENABLE_RIGHT_BUTTON.get()
         || mc.player.isCrouching() || isAnimationGui(mc.screen)) {
         return false;
      }

      ItemStack stack = mc.player.getMainHandItem();
      if (!FoodDefinitionManager.init_ItemList.contains(stack.getItem())
         || Empty.isClientAnimationPlaying(stack, "switchin")) {
         return false;
      }

      if (!rightClickHandled) {
         GameplayEvents.startRightClickAnimation(stack);
         rightClickHandled = true;
      }
      return true;
   }

   private static void onClientTickStart(Minecraft mc) {
      if (mc.player == null || !mc.options.keyUse.isDown()) {
         rightClickHandled = false;
      }

      if (mc.player == null || isAnimationGui(mc.screen) || !AnimationUtils.isAnimationLocked()) {
         return;
      }

      if (mc.options.keyAttack.consumeClick()) {
         AnimationUtils.stopAnimationControl();
         mc.player.swing(InteractionHand.MAIN_HAND);
      }
   }

   private static void onClientTick(Minecraft mc) {
      if (mc.player == null) {
         AnimationUtils.clearAnimationState();
         return;
      }

      if (ModKeyMappings.ANIMATION.consumeClick()) {
         startKeyboardAnimation(mc);
      }

      if (Food.DEBUG) {
         updateDebugKeys(mc);
      }

      AnimationUtils.updateCustomUseState(mc);
      if (!isAnimationGui(mc.screen)) {
         AnimationUtils.cancelIfHeldItemChanged(mc);
      }

      if ((mc.screen == null || isAnimationGui(mc.screen)) && mc.options.getCameraType().isFirstPerson()) {
         AnimationUtils.checkEatAnimationState();
         if (AnimationUtils.isAnimationLocked()) {
            mc.player.getInventory().selected = AnimationUtils.lockedHotbarSlot;
         } else {
            AnimationUtils.lockedHotbarSlot = -1;
         }
      } else if (AnimationUtils.isAnimationLocked()) {
         AnimationUtils.stopAnimationControl();
      }

   }

   private static void startKeyboardAnimation(Minecraft mc) {
      if (AnimationUtils.isAnimationLocked() || AnimationUtils.isAnimationRequestPending() || mc.player == null) {
         return;
      }

      ItemStack stack = mc.player.getMainHandItem();
      if (!Empty.isClientAnimationPlaying(stack, "switchin")
         && FoodDefinitionManager.init_ItemList.contains(stack.getItem())) {
         AnimationUtils.beginAnimationSession(mc);
         ClientNetwork.sendAnimation(AnimationUtils.getAnimationSessionId(mc));
      }
   }

   private static void updateDebugKeys(Minecraft mc) {
      if (mc.options.keyInventory.consumeClick()) {
         debugX += 0.05F;
      }
      if (mc.options.keyAdvancements.consumeClick()) {
         debugX -= 0.05F;
      }
      if (mc.options.keyDrop.consumeClick()) {
         debugY -= 0.05F;
      }
      if (mc.options.keySwapOffhand.consumeClick()) {
         debugZ -= 0.05F;
      }
      if (mc.options.keyChat.consumeClick()) {
         debugRo += 0.01F;
      }
      if (mc.options.keyCommand.consumeClick()) {
         debugRo -= 0.01F;
      }
      if (mc.options.keyPlayerList.consumeClick()) {
         debugY += 0.05F;
      }
      if (mc.options.keyScreenshot.consumeClick()) {
         debugZ += 0.05F;
      }

      KaleidoscopeImmersiveEating.LOGGER.debug("translate = {}, {}, {}",debugX, debugY, debugZ);
   }

   private static boolean isAnimationGui(Screen screen) {
      return screen instanceof ChatScreen || screen instanceof AbstractContainerScreen;
   }
}

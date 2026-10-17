package io.github.memorytoame.immersiveeating.fabric.client.events;

import io.github.memorytoame.immersiveeating.fabric.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.fabric.common.content.item.Empty;
import io.github.memorytoame.immersiveeating.fabric.client.network.ClientNetwork;
import io.github.memorytoame.immersiveeating.fabric.utils.AnimationUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public final class GameplayEvents {
   private GameplayEvents() {
   }

   public static boolean startRightClickAnimation() {
      Minecraft mc = Minecraft.getInstance();
      return mc.player != null && startRightClickAnimation(mc.player.getMainHandItem());
   }

   public static boolean startRightClickAnimation(ItemStack stack) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && !AnimationUtils.isAnimationLocked() && !AnimationUtils.isAnimationRequestPending()
         && !Empty.isClientAnimationPlaying(stack, "switchin") && FoodDefinitionManager.init_ItemList.contains(stack.getItem())) {
         AnimationUtils.beginAnimationSession(mc);
         ClientNetwork.sendAnimation(AnimationUtils.getAnimationSessionId(mc));
         return true;
      }
      return false;
   }
}


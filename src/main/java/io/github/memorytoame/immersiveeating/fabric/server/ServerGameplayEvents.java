package io.github.memorytoame.immersiveeating.fabric.server;

import io.github.memorytoame.immersiveeating.fabric.common.config.Config;
import io.github.memorytoame.immersiveeating.fabric.common.content.definition.FoodDefinitionManager;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.InteractionResultHolder;

public final class ServerGameplayEvents {
   private ServerGameplayEvents() {
   }

   public static void register() {
      ServerAnimationEvents.register();
      UseItemCallback.EVENT.register((player, level, hand) -> {
         if (!level.isClientSide() && Config.ENABLE_RIGHT_BUTTON.get() && FoodDefinitionManager.init_ItemList.contains(player.getItemInHand(hand).getItem())) {
            return InteractionResultHolder.fail(player.getItemInHand(hand));
         }
         return InteractionResultHolder.pass(player.getItemInHand(hand));
      });
   }
}


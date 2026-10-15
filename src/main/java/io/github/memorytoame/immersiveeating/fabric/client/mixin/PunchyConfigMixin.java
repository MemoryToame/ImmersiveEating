package io.github.memorytoame.immersiveeating.fabric.client.mixin;

import io.github.memorytoame.immersiveeating.fabric.common.content.definition.FoodDefinitionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "punchy.config.PunchyConfig")
public class PunchyConfigMixin {
   @Inject(method = "isModEnabled()Z", at = @At("HEAD"), cancellable = true, require = 0)
   private static void food$disablePunchyForCustomItem(CallbackInfoReturnable<Boolean> cir) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         if (isCustomItem(mc.player.getMainHandItem()) && !mc.player.isUsingItem()) {
            cir.setReturnValue(false);
         }
      }
   }

   @Unique
   private static boolean isCustomItem(ItemStack stack) {
      return !stack.isEmpty() && FoodDefinitionManager.init_ItemList.contains(stack.getItem());
   }
}


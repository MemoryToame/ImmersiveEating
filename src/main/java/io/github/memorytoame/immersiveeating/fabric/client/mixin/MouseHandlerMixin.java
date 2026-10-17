package io.github.memorytoame.immersiveeating.fabric.client.mixin;

import io.github.memorytoame.immersiveeating.fabric.utils.AnimationUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
   @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
   private void food$blockScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
      if (AnimationUtils.isInputLocked()) {
         ci.cancel();
      }
   }
}

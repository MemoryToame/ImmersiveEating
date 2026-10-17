package io.github.memorytoame.immersiveeating.fabric.client.mixin;

import io.github.memorytoame.immersiveeating.fabric.utils.AnimationUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
   @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
   private void food$blockHotbarKeys(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
      if (AnimationUtils.isInputLocked() && !food$isAllowedDuringAnimation(key, scanCode)) {
         ci.cancel();
      }
   }

   @Unique
   private static boolean food$isAllowedDuringAnimation(int key, int scanCode) {
      Options options = Minecraft.getInstance().options;
      return options.keyAttack.matches(key, scanCode)
         || options.keyUp.matches(key, scanCode)
         || options.keyDown.matches(key, scanCode)
         || options.keyLeft.matches(key, scanCode)
         || options.keyRight.matches(key, scanCode)
         || options.keyJump.matches(key, scanCode);
   }
}


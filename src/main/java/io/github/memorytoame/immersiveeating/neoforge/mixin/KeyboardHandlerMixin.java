package io.github.memorytoame.immersiveeating.neoforge.mixin;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    /*
    动画期间禁止切换物品
     */
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void food$blockHotbarKeys(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (Food.lockedHotbarSlot >= 0 && key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_9) {
            ci.cancel();
        }
    }
}

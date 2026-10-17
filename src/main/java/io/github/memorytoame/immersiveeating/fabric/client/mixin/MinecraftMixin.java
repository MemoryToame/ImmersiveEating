package io.github.memorytoame.immersiveeating.fabric.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.memorytoame.immersiveeating.fabric.client.input.InputEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
   @WrapOperation(
      method = "handleKeybinds",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;startUseItem()V")
   )
   private void food$handleRightClick(Minecraft mc, Operation<Void> original) {
      if (!io.github.memorytoame.immersiveeating.fabric.utils.AnimationUtils.isInputLocked() && !InputEvents.handleRightClick(mc)) {
         original.call(mc);
      }
   }
}

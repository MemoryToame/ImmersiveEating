package io.github.memorytoame.immersiveeating.fabric.client.mixin;

import io.github.memorytoame.immersiveeating.fabric.client.animation.CameraRotationComposer;
import io.github.memorytoame.immersiveeating.fabric.client.animation.CameraShake;
import io.github.memorytoame.immersiveeating.fabric.common.content.item.Empty;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin {
   @Inject(
      method = "setup",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/Camera;setPosition(DDD)V",
         shift = At.Shift.BEFORE
      )
   )
   private void food$applyAnimationRotation(
      BlockGetter level,
      Entity entity,
      boolean detached,
      boolean mirrored,
      float partialTick,
      CallbackInfo ci
   ) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || detached || !mc.options.getCameraType().isFirstPerson()) {
         CameraRotationComposer.clearBlockbenchRotation();
         return;
      }

      boolean animationPlaying = Empty.isEatAnimationPlaying();
      if (!animationPlaying) {
         CameraRotationComposer.clearBlockbenchRotation();
      }

      CameraRotationComposer.applyCameraEffects(
         (Camera)(Object)this,
         animationPlaying,
         CameraShake.interpolatedPitch(partialTick),
         CameraShake.interpolatedYaw(partialTick),
         CameraShake.interpolatedRoll(partialTick)
      );
   }
}

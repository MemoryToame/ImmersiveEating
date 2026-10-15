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
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Camera.class)
public abstract class CameraMixin {
   @Final
   @Shadow
   private Quaternionf rotation;

   @Inject(method = "setup", at = @At("TAIL"))
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

      if (Empty.isEatAnimationPlaying()) {
         CameraRotationComposer.applyCapturedBlockbenchRotation(this.rotation);
      } else {
         CameraRotationComposer.clearBlockbenchRotation();
      }

      CameraRotationComposer.applyForgeOffset(
         this.rotation,
         CameraShake.interpolatedPitch(partialTick),
         CameraShake.interpolatedYaw(partialTick),
         CameraShake.interpolatedRoll(partialTick)
      );
   }
}

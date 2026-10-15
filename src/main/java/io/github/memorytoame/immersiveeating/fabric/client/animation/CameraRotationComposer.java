package io.github.memorytoame.immersiveeating.fabric.client.animation;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class CameraRotationComposer {
   private static final float DEG_TO_RAD = (float)(Math.PI / 180.0);
   private static final float RAD_TO_DEG = (float)(180.0 / Math.PI);
   private static final float GIMBAL_LOCK_EPSILON = 0.001745F;
   private static final float MAX_CAMERA_PITCH_RADIANS = 1.569051F;
   private static float blockbenchX;
   private static float blockbenchY;
   private static float blockbenchZ;
   private static boolean hasBlockbenchRotation;

   private CameraRotationComposer() {
   }

   public static void captureBlockbenchRotationRadians(float xRadians, float yRadians, float zRadians) {
      blockbenchX = xRadians;
      blockbenchY = yRadians;
      blockbenchZ = zRadians;
      hasBlockbenchRotation = true;
   }

   public static void clearBlockbenchRotation() {
      blockbenchX = 0.0F;
      blockbenchY = 0.0F;
      blockbenchZ = 0.0F;
      hasBlockbenchRotation = false;
   }

   public static void applyCapturedBlockbenchRotation(Quaternionf cameraRotation) {
      if (hasBlockbenchRotation) {
         applyLocalCameraRotation(
            cameraRotation,
            blockbenchX * RAD_TO_DEG,
            blockbenchY * RAD_TO_DEG,
            blockbenchZ * RAD_TO_DEG
         );
      }
   }

   public static void applyForgeOffset(Quaternionf cameraRotation, float pitchDegrees, float yawDegrees, float rollDegrees) {
      applyLocalCameraRotation(cameraRotation, -pitchDegrees, -yawDegrees, -rollDegrees);
   }

   private static void applyLocalCameraRotation(Quaternionf cameraRotation, float xDegrees, float yDegrees, float zDegrees) {
      Quaternionf localCameraRotation = new Quaternionf()
         .rotationX(xDegrees * DEG_TO_RAD)
         .rotateY(yDegrees * DEG_TO_RAD)
         .rotateZ(zDegrees * DEG_TO_RAD);
      cameraRotation.set(localCameraRotation.conjugate().mul(cameraRotation));
   }

}


package io.github.memorytoame.immersiveeating.fabric.client.animation;

import io.github.memorytoame.immersiveeating.fabric.client.mixin.accessor.CameraAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

@Environment(EnvType.CLIENT)
public final class CameraRotationComposer {
   private static final float DEG_TO_RAD = (float)(Math.PI / 180.0);
   private static final float RAD_TO_DEG = (float)(180.0 / Math.PI);
   private static final float GIMBAL_LOCK_EPSILON = 0.001745F;
   private static final float MAX_CAMERA_PITCH_RADIANS = 1.569051F;
   private static final Quaternionf BASE_VIEW_ROTATION = new Quaternionf();
   private static final Quaternionf LOCAL_CAMERA_ROTATION = new Quaternionf();
   private static final Matrix3f FINAL_VIEW_MATRIX = new Matrix3f();
   private static float extractedPitchDegrees;
   private static float extractedYawDegrees;
   private static float extractedRollDegrees;
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

   /**
    * Applies the same local-camera composition used by NeoForge's
    * {@code ViewportEvent.ComputeCameraAngles} hook.
    *
    * <p>Fabric has no equivalent viewport event.  The camera is already set up
    * when this method runs, so the vanilla angles are rebuilt into the same
    * base quaternion. Each local rotation is converted back through the same
    * Euler-angle decomposition NeoForge uses before the next one is applied.</p>
    */
   public static void applyCameraEffects(
      Camera camera,
      boolean applyBlockbench,
      float pitchDegrees,
      float yawDegrees,
      float rollDegrees
   ) {
      float vanillaYaw = camera.getYRot();
      Quaternionf baseViewRotation = BASE_VIEW_ROTATION
         .rotationZ(0.0F)
         .rotateX(camera.getXRot() * DEG_TO_RAD)
         .rotateY((vanillaYaw + 180.0F) * DEG_TO_RAD);
      float fallbackYaw = vanillaYaw;

      if (applyBlockbench && hasBlockbenchRotation) {
         applyLocalCameraRotation(
            baseViewRotation,
            blockbenchX * RAD_TO_DEG,
            blockbenchY * RAD_TO_DEG,
            blockbenchZ * RAD_TO_DEG
         );
         extractCameraAngles(baseViewRotation, fallbackYaw);
         fallbackYaw = extractedYawDegrees;
         baseViewRotation
            .rotationZ(extractedRollDegrees * DEG_TO_RAD)
            .rotateX(extractedPitchDegrees * DEG_TO_RAD)
            .rotateY((extractedYawDegrees + 180.0F) * DEG_TO_RAD);
      }

      applyLocalCameraRotation(baseViewRotation, -pitchDegrees, -yawDegrees, -rollDegrees);
      writeCameraRotation(camera, baseViewRotation, fallbackYaw);
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
      Quaternionf localCameraRotation = LOCAL_CAMERA_ROTATION
         .rotationX(xDegrees * DEG_TO_RAD)
         .rotateY(yDegrees * DEG_TO_RAD)
         .rotateZ(zDegrees * DEG_TO_RAD);
      cameraRotation.set(localCameraRotation.conjugate().mul(cameraRotation));
   }

   private static void writeCameraRotation(Camera camera, Quaternionf finalViewRotation, float fallbackYaw) {
      extractCameraAngles(finalViewRotation, fallbackYaw);
      float yaw = extractedYawDegrees;
      float pitch = extractedPitchDegrees;
      float roll = extractedRollDegrees;

      // Keep Camera#getXRot/getYRot in sync with the composed quaternion. The
      // instanceof guard keeps the pure JUnit tests usable without a Mixin runtime.
      if (camera instanceof CameraAccessor accessor) {
         accessor.food$setRotation(yaw, pitch);
      }

      Quaternionf cameraRotation = camera.rotation();
      cameraRotation.rotationYXZ(
         (float)Math.PI - yaw * DEG_TO_RAD,
         -pitch * DEG_TO_RAD,
         -roll * DEG_TO_RAD
      );

      // Camera#setRotation also refreshes these vectors. We update them here
      // because Fabric applies the event equivalent after vanilla setup.
      camera.getLookVector().set(0.0F, 0.0F, -1.0F).rotate(cameraRotation);
      camera.getUpVector().set(0.0F, 1.0F, 0.0F).rotate(cameraRotation);
      camera.getLeftVector().set(-1.0F, 0.0F, 0.0F).rotate(cameraRotation);
   }

   private static void extractCameraAngles(Quaternionf finalViewRotation, float fallbackYaw) {
      Matrix3f matrix = FINAL_VIEW_MATRIX.rotation(finalViewRotation);
      float pitchRadians = (float)Math.asin(Mth.clamp(matrix.m12(), -1.0F, 1.0F));
      float eventPitchRadians = Mth.clamp(
         pitchRadians,
         -MAX_CAMERA_PITCH_RADIANS,
         MAX_CAMERA_PITCH_RADIANS
      );
      float cosPitch = (float)Math.cos(pitchRadians);
      float rollRadians;
      float yawWithOffsetRadians;

      if (Math.abs(cosPitch) > GIMBAL_LOCK_EPSILON) {
         rollRadians = (float)Math.atan2(-matrix.m10(), matrix.m11());
         yawWithOffsetRadians = (float)Math.atan2(-matrix.m02(), matrix.m22());
      } else {
         yawWithOffsetRadians = (fallbackYaw + 180.0F) * DEG_TO_RAD;
         float coupledAngle = (float)Math.atan2(matrix.m01(), matrix.m00());
         rollRadians = pitchRadians >= 0.0F
            ? coupledAngle - yawWithOffsetRadians
            : coupledAngle + yawWithOffsetRadians;
      }

      extractedYawDegrees = Mth.wrapDegrees(yawWithOffsetRadians * RAD_TO_DEG - 180.0F);
      extractedPitchDegrees = eventPitchRadians * RAD_TO_DEG;
      extractedRollDegrees = rollRadians * RAD_TO_DEG;
   }

}


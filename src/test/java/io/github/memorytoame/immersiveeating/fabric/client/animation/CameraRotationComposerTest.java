package io.github.memorytoame.immersiveeating.fabric.client.animation;

import net.minecraft.client.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CameraRotationComposerTest {
   @AfterEach
   void clearCapturedRotation() {
      CameraRotationComposer.clearBlockbenchRotation();
   }

   @Test
   void cameraBasisMatchesComposedRotation() {
      Camera camera = new Camera();

      CameraRotationComposer.applyCameraEffects(camera, false, 3.5F, 0.5F, 1.2F);

      Quaternionf rotation = camera.rotation();
      Vector3f expectedLook = new Vector3f(0.0F, 0.0F, -1.0F).rotate(rotation);
      Vector3f expectedUp = new Vector3f(0.0F, 1.0F, 0.0F).rotate(rotation);
      Vector3f expectedLeft = new Vector3f(-1.0F, 0.0F, 0.0F).rotate(rotation);

      assertEquals(expectedLook.x, camera.getLookVector().x, 1.0E-5F);
      assertEquals(expectedLook.y, camera.getLookVector().y, 1.0E-5F);
      assertEquals(expectedLook.z, camera.getLookVector().z, 1.0E-5F);
      assertEquals(expectedUp.x, camera.getUpVector().x, 1.0E-5F);
      assertEquals(expectedUp.y, camera.getUpVector().y, 1.0E-5F);
      assertEquals(expectedUp.z, camera.getUpVector().z, 1.0E-5F);
      assertEquals(expectedLeft.x, camera.getLeftVector().x, 1.0E-5F);
      assertEquals(expectedLeft.y, camera.getLeftVector().y, 1.0E-5F);
      assertEquals(expectedLeft.z, camera.getLeftVector().z, 1.0E-5F);
      assertEquals(1.0F, rotation.lengthSquared(), 1.0E-5F);
   }

   @Test
   void blockbenchRotationIsAppliedOnlyWhileRequested() {
      Camera withoutAnimation = new Camera();
      Camera withAnimation = new Camera();
      CameraRotationComposer.captureBlockbenchRotationRadians(0.15F, -0.1F, 0.2F);

      CameraRotationComposer.applyCameraEffects(withAnimation, true, 0.0F, 0.0F, 0.0F);
      CameraRotationComposer.applyCameraEffects(withoutAnimation, false, 0.0F, 0.0F, 0.0F);

      assertNotEquals(
         1.0F,
         Math.abs(withAnimation.rotation().dot(withoutAnimation.rotation())),
         1.0E-3F
      );

      Camera cleared = new Camera();
      CameraRotationComposer.clearBlockbenchRotation();
      CameraRotationComposer.applyCameraEffects(cleared, true, 0.0F, 0.0F, 0.0F);
      assertTrue(Math.abs(cleared.rotation().dot(withoutAnimation.rotation())) > 0.999F);
   }
}

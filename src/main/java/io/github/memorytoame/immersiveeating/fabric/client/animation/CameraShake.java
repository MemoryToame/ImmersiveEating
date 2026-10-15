package io.github.memorytoame.immersiveeating.fabric.client.animation;

import java.util.concurrent.ThreadLocalRandom;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public final class CameraShake {
   private static float yaw;
   private static float pitch;
   private static float roll;
   private static float yawVel;
   private static float pitchVel;
   private static float rollVel;
   private static float prevYaw;
   private static float prevPitch;
   private static float prevRoll;

   private CameraShake() {
   }

   public static void trigger() {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      pitchVel += random.nextBoolean() ? random.nextFloat(3.2F, 4.0F) : -random.nextFloat(3.2F, 4.0F);
      yawVel += random.nextBoolean() ? random.nextFloat(0.2F, 0.6F) : -random.nextFloat(0.2F, 0.6F);
      rollVel += random.nextBoolean() ? random.nextFloat(1.5F, 2.1F) : -random.nextFloat(1.5F, 2.1F);
   }

   public static void tickClient(Minecraft mc) {
      if (mc.player == null) {
         reset();
         return;
      }
      prevYaw = yaw;
      prevPitch = pitch;
      prevRoll = roll;
      yawVel = (yawVel - yaw * 0.25F) * 0.68F;
      pitchVel = (pitchVel - pitch * 0.25F) * 0.68F;
      rollVel = (rollVel - roll * 0.25F) * 0.68F;
      yaw = Mth.clamp(yaw + yawVel, -2.0F, 2.0F);
      pitch = Mth.clamp(pitch + pitchVel, -4.0F, 4.0F);
      roll = Mth.clamp(roll + rollVel, -2.0F, 2.0F);
      HeldItemMotion.tickInertia();
      HeldItemMotion.tickJump();
      HeldItemMotion.tickCrouch();
   }

   public static void reset() {
      CameraRotationComposer.clearBlockbenchRotation();
      yaw = pitch = roll = yawVel = pitchVel = rollVel = 0.0F;
      prevYaw = prevPitch = prevRoll = 0.0F;
   }

   public static float interpolatedYaw(float partialTick) {
      return Mth.lerp(partialTick, prevYaw, yaw);
   }

   public static float interpolatedPitch(float partialTick) {
      return Mth.lerp(partialTick, prevPitch, pitch);
   }

   public static float interpolatedRoll(float partialTick) {
      return Mth.lerp(partialTick, prevRoll, roll);
   }
}


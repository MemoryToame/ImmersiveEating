package io.github.memorytoame.immersiveeating.fabric.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.memorytoame.immersiveeating.fabric.utils.AnimationUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class HeldItemMotion {
   private static Vec3 prevHorizontalVel = Vec3.ZERO;
   private static float inertiaPitch;
   private static float inertiaPitchVel;
   private static float inertiaRoll;
   private static float inertiaRollVel;
   private static float armInertiaPitch;
   private static float armInertiaPitchVel;
   private static float armInertiaRoll;
   private static float armInertiaRollVel;
   private static boolean wasOnGround = true;
   private static float prevVerticalVel;
   private static float jumpY;
   private static float jumpYVel;
   private static float jumpPitch;
   private static float jumpPitchVel;
   private static float prevInertiaPitch;
   private static float prevInertiaRoll;
   private static float prevArmInertiaPitch;
   private static float prevArmInertiaRoll;
   private static float prevJumpY;
   private static float prevJumpPitch;
   private static float crouchAmount;
   private static boolean wasCrouching;
   private static float crouchKickPitch;
   private static float crouchKickPitchVel;
   private static float crouchKickRoll;
   private static float crouchKickRollVel;
   private static float prevCrouchKickPitch;
   private static float prevCrouchKickRoll;

   private HeldItemMotion() {
   }

   public static void applyIdleMotion(PoseStack poseStack, float partialTick) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         float time = mc.player.tickCount + partialTick;
         float x = Mth.sin(time * 0.075F) * 0.006F;
         float y = Mth.cos(time * 0.105F) * 0.004F;
         float pitch = Mth.cos(time * 0.06F) * 0.35F;
         float roll = Mth.sin(time * 0.085F) * 0.5F;
         poseStack.translate(x, y, 0.0F);
         poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
         poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
      }
   }

   public static void applyWalkMotion(PoseStack poseStack, float partialTick) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         Player p = mc.player;
         float phase = p.walkAnimation.position(partialTick);
         float amount = Mth.clamp(p.walkAnimation.speed(partialTick) * 1.35F, 0.0F, 1.0F);
         if (!p.onGround()) {
            amount *= 0.2F;
         }

         if (AnimationUtils.isAnimationLocked()) {
            amount *= 0.15F;
         }

         float x = Mth.sin(phase) * 0.003F * amount;
         float y = Mth.cos(phase * 2.0F) * 0.0025F * amount;
         float pitch = Mth.cos(phase * 2.0F) * 0.05F * amount;
         float roll = Mth.sin(phase) * 0.035F * amount;
         poseStack.translate(x, y, 0.0F);
         poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
         poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
      }
   }

   public static void applyInertiaMotion(PoseStack poseStack, float partialTick) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         float pitch = Mth.lerp(partialTick, prevInertiaPitch, inertiaPitch);
         float roll = Mth.lerp(partialTick, prevInertiaRoll, inertiaRoll);
         float intensity = mc.player.isUsingItem() ? 0.15F : 1.0F;
         float pitchIntensity = intensity * 0.5F;
         poseStack.mulPose(Axis.XP.rotationDegrees(pitch * pitchIntensity));
         poseStack.mulPose(Axis.ZP.rotationDegrees(roll * intensity));
      }
   }

   public static void tickInertia() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         resetInertia();
      } else {
         prevInertiaPitch = inertiaPitch;
         prevInertiaRoll = inertiaRoll;
         prevArmInertiaPitch = armInertiaPitch;
         prevArmInertiaRoll = armInertiaRoll;
         Player player = mc.player;
         Vec3 vel = player.getDeltaMovement();
         Vec3 currHorizontalVel = new Vec3(vel.x, 0.0, vel.z);
         Vec3 acceleration = currHorizontalVel.subtract(prevHorizontalVel);
         float yaw = player.getYRot() * (float) (Math.PI / 180.0);
         Vec3 forward = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
         Vec3 right = new Vec3(Mth.cos(yaw), 0.0, Mth.sin(yaw));
         float forwardAcceleration = (float)acceleration.dot(forward);
         float sideAcceleration = (float)acceleration.dot(right);
         inertiaPitchVel -= forwardAcceleration * 16.0F;
         inertiaRollVel += sideAcceleration * 10.0F;
         inertiaPitchVel = inertiaPitchVel + -inertiaPitch * 0.2F;
         inertiaPitchVel *= 0.72F;
         inertiaPitch = inertiaPitch + inertiaPitchVel;
         inertiaRollVel = inertiaRollVel + -inertiaRoll * 0.2F;
         inertiaRollVel *= 0.72F;
         inertiaRoll = inertiaRoll + inertiaRollVel;
         inertiaPitch = Mth.clamp(inertiaPitch, -8.0F, 8.0F);
         inertiaRoll = Mth.clamp(inertiaRoll, -6.0F, 6.0F);
         prevHorizontalVel = currHorizontalVel;
         armInertiaPitchVel -= forwardAcceleration * 8.0F;
         armInertiaRollVel += sideAcceleration * 5.0F;
         armInertiaPitchVel = armInertiaPitchVel + -armInertiaPitch * 0.12F;
         armInertiaPitchVel *= 0.45F;
         armInertiaPitch = armInertiaPitch + armInertiaPitchVel;
         armInertiaRollVel = armInertiaRollVel + -armInertiaRoll * 0.12F;
         armInertiaRollVel *= 0.45F;
         armInertiaRoll = armInertiaRoll + armInertiaRollVel;
         armInertiaPitch = Mth.clamp(armInertiaPitch, -3.5F, 3.5F);
         armInertiaRoll = Mth.clamp(armInertiaRoll, -2.5F, 2.5F);
      }
   }

   public static void resetInertia() {
      prevHorizontalVel = Vec3.ZERO;
      inertiaPitch = 0.0F;
      inertiaPitchVel = 0.0F;
      inertiaRoll = 0.0F;
      inertiaRollVel = 0.0F;
      armInertiaPitch = 0.0F;
      armInertiaPitchVel = 0.0F;
      armInertiaRoll = 0.0F;
      armInertiaRollVel = 0.0F;
   }

   public static void tickJump() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         resetJump();
      } else {
         prevJumpY = jumpY;
         prevJumpPitch = jumpPitch;
         Player player = mc.player;
         Vec3 vel = player.getDeltaMovement();
         boolean onGround = player.onGround();
         if (wasOnGround && !onGround && vel.y > 0.05) {
            jumpYVel += 0.0025F;
            jumpPitchVel -= 0.1F;
         }

         if (!wasOnGround && onGround && prevVerticalVel < -0.05F) {
            float impact = Mth.clamp(-prevVerticalVel * 1.5F, 0.0F, 1.0F);
            jumpYVel -= 0.025F + impact * 0.035F;
            jumpPitchVel += 0.25F + impact * 0.45F;
         }

         jumpYVel = jumpYVel + -jumpY * 0.16F;
         jumpYVel *= 0.78F;
         jumpY = jumpY + jumpYVel;
         jumpPitchVel = jumpPitchVel + -jumpPitch * 0.18F;
         jumpPitchVel *= 0.76F;
         jumpPitch = jumpPitch + jumpPitchVel;
         jumpY = Mth.clamp(jumpY, -0.08F, 0.08F);
         jumpPitch = Mth.clamp(jumpPitch, -5.0F, 0.65F);
         prevVerticalVel = (float)vel.y;
         wasOnGround = onGround;
      }
   }

   public static void resetJump() {
      wasOnGround = true;
      prevVerticalVel = 0.0F;
      jumpY = 0.0F;
      jumpYVel = 0.0F;
      jumpPitch = 0.0F;
      jumpPitchVel = 0.0F;
   }

   public static void applyJumpMotion(PoseStack poseStack, float partialTick) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         float y = Mth.lerp(partialTick, prevJumpY, jumpY);
         float pitch = Mth.lerp(partialTick, prevJumpPitch, jumpPitch);
         float intensity = mc.player.isUsingItem() ? 0.15F : 1.0F;
         float verticalIntensity = intensity / 3.0F;
         float pitchIntensity = mc.player.isSprinting() && !mc.player.onGround() ? intensity / 12.0F : intensity;
         poseStack.translate(0.0F, y * verticalIntensity, 0.0F);
         poseStack.mulPose(Axis.XP.rotationDegrees(pitch * pitchIntensity));
      }
   }

   public static void applyCrouchMotion(PoseStack poseStack, float partialTick) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         Player player = mc.player;
         float target = player.isCrouching() ? 1.0F : 0.0F;
         crouchAmount = Mth.lerp(0.18F, crouchAmount, target);
         if (crouchAmount < 0.001F) {
            crouchAmount = 0.0F;
         } else {
            float time = player.tickCount + partialTick;
            float movement = Mth.clamp(player.walkAnimation.speed(partialTick) * 1.25F, 0.0F, 1.0F);
            float intensity = crouchAmount * (0.35F + movement * 0.65F);
            float x = Mth.sin(time * 0.2F) * 0.0025F * intensity;
            float y = Mth.cos(time * 0.4F) * 0.0015F * intensity;
            float pitch = Mth.cos(time * 0.2F) * 0.55F * intensity;
            float roll = Mth.sin(time * 0.2F) * 1.15F * intensity;
            float kickPitch = Mth.lerp(partialTick, prevCrouchKickPitch, crouchKickPitch);
            float kickRoll = Mth.lerp(partialTick, prevCrouchKickRoll, crouchKickRoll);
            poseStack.translate(x, y, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
            poseStack.mulPose(Axis.XP.rotationDegrees(kickPitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(kickRoll));
         }
      }
   }

   public static void tickCrouch() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         resetCrouch();
      } else {
         prevCrouchKickPitch = crouchKickPitch;
         prevCrouchKickRoll = crouchKickRoll;
         boolean crouching = mc.player.isCrouching();
         if (!wasCrouching && crouching) {
            crouchKickPitchVel -= 0.75F;
            crouchKickRollVel += 0.3F;
         }

         crouchKickPitchVel = crouchKickPitchVel + -crouchKickPitch * 0.24F;
         crouchKickPitchVel *= 0.7F;
         crouchKickPitch = crouchKickPitch + crouchKickPitchVel;
         crouchKickRollVel = crouchKickRollVel + -crouchKickRoll * 0.24F;
         crouchKickRollVel *= 0.7F;
         crouchKickRoll = crouchKickRoll + crouchKickRollVel;
         crouchKickPitch = Mth.clamp(crouchKickPitch, -2.0F, 2.0F);
         crouchKickRoll = Mth.clamp(crouchKickRoll, -1.0F, 1.0F);
         wasCrouching = crouching;
      }
   }

   public static void resetCrouch() {
      wasCrouching = false;
      crouchKickPitch = 0.0F;
      crouchKickPitchVel = 0.0F;
      crouchKickRoll = 0.0F;
      crouchKickRollVel = 0.0F;
      prevCrouchKickPitch = 0.0F;
      prevCrouchKickRoll = 0.0F;
   }
}


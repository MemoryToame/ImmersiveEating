package io.github.memorytoame.immersiveeating.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.memorytoame.immersiveeating.neoforge.utils.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;



public class HeldItemMotion {
    /*
    idleMotion       自然晃动
    walkMotion       走路晃动
    inertiaMotion    惯性晃动
    jumpMotion       跳跃晃动
     */
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

        if (mc.player == null) {
            return;
        }

        float time = mc.player.tickCount + partialTick;

        float x = Mth.sin(time * 0.075F) * 0.006F;// 左右移动
        float y = Mth.cos(time * 0.105F) * 0.004F;// 上下移动

        float pitch = Mth.cos(time * 0.060F) * 0.35F;// 前后倾斜
        float roll = Mth.sin(time * 0.085F) * 0.50F;// 左右旋转

        poseStack.translate(x, y, 0.0F);

        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
    }

    public static void applyWalkMotion(PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Player p = mc.player;

        float phase = p.walkAnimation.position(partialTick);
        //System.out.println(phase);
        float amount = Mth.clamp(p.walkAnimation.speed(partialTick) * 1.35F, 0.0F, 1.0F);
        //System.out.println(amount);

        // 在空气中应用
        if (!p.onGround()) {
            amount *= 0.2F;
        }

        // 去除播放动画时候应用
        if (AnimationUtils.isAnimationLocked()) {
            amount *= 0.15F;
        }
        //系数越大 效果越明显
        float x = Mth.sin(phase) * 0.003F * amount;
        float y = Mth.cos(phase * 2.0F) * 0.0025F * amount;

        float pitch = Mth.cos(phase * 2.0F) * 0.05F * amount;//弹性
        float roll = Mth.sin(phase) * 0.035F * amount;

        poseStack.translate(x, y, 0.0F);

        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
    }

    //惯性运动
    public static void applyInertiaMotion(PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        float pitch = Mth.lerp(partialTick, prevInertiaPitch, inertiaPitch);

        float roll = Mth.lerp(partialTick, prevInertiaRoll, inertiaRoll);

        //摇晃强度
        float intensity = mc.player.isUsingItem() ? 0.15F : 1.0F;

        poseStack.mulPose(Axis.XP.rotationDegrees(pitch * intensity));

        poseStack.mulPose(Axis.ZP.rotationDegrees(roll * intensity));
    }

    public static void tickInertia() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            resetInertia();
            return;
        }
        prevInertiaPitch = inertiaPitch;
        prevInertiaRoll = inertiaRoll;
        prevArmInertiaPitch = armInertiaPitch;
        prevArmInertiaRoll = armInertiaRoll;
        Player player = mc.player;
        //get运动矢量
        Vec3 vel = player.getDeltaMovement();

        //水平矢量
        Vec3 currHorizontalVel = new Vec3(vel.x, 0.0, vel.z);

        //水平加速度
        Vec3 acceleration = currHorizontalVel.subtract(prevHorizontalVel);

        float yaw = player.getYRot() * Mth.DEG_TO_RAD;

        // 前进矢量
        Vec3 forward = new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));

        //向右矢量
        Vec3 right = new Vec3(Mth.cos(yaw), 0.0, Mth.sin(yaw));

        float forwardAcceleration = (float) acceleration.dot(forward);
        float sideAcceleration = (float) acceleration.dot(right);

        /*
         前进加速度：
         物品会落后于玩家

         突然停止
         加速度变为负值，
         物品会向前摆动。
        */
        //16f 10f为系数 越大惯性越大
        inertiaPitchVel -= forwardAcceleration * 16.0F;
        //侧向移动会产生侧倾
        inertiaRollVel += sideAcceleration * 10.0F;

        //弹回到初始位置
        inertiaPitchVel += -inertiaPitch * 0.20F;
        inertiaPitchVel *= 0.72F;
        inertiaPitch += inertiaPitchVel;

        inertiaRollVel += -inertiaRoll * 0.20F;
        inertiaRollVel *= 0.72F;
        inertiaRoll += inertiaRollVel;

        inertiaPitch = Mth.clamp(inertiaPitch, -8.0F, 8.0F);

        inertiaRoll = Mth.clamp(inertiaRoll, -6.0F, 6.0F);
        prevHorizontalVel = currHorizontalVel;


        armInertiaPitchVel -= forwardAcceleration *8.0F;
        armInertiaRollVel += sideAcceleration * 5.0F;

        armInertiaPitchVel += -armInertiaPitch * 0.12F;
        armInertiaPitchVel *= 0.45F;
        armInertiaPitch += armInertiaPitchVel;

        armInertiaRollVel += -armInertiaRoll * 0.12F;
        armInertiaRollVel *= 0.45F;
        armInertiaRoll += armInertiaRollVel;

        armInertiaPitch = Mth.clamp(armInertiaPitch, -3.5F, 3.5F);
        armInertiaRoll = Mth.clamp(armInertiaRoll, -2.5F, 2.5F);
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

    public static void applyArmInertiaMotion(PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        float pitch = Mth.lerp(partialTick, prevArmInertiaPitch, armInertiaPitch);
        float roll = Mth.lerp(partialTick, prevArmInertiaRoll, armInertiaRoll);
        float intensity = AnimationUtils.isAnimationLocked() ? 0.35F : 0.5F;
        float walkPhase = mc.player.walkAnimation.position(partialTick);
        float walkAmount = Mth.clamp(mc.player.walkAnimation.speed(partialTick) * 1.1F, 0.0F, 1.0F);
        //摇晃太多 改周期
        float z = Mth.cos(walkPhase*0.6F + 0.6F) * 0.0075F * walkAmount * intensity;

        poseStack.translate(0.0F, 0.0F, z);
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch * intensity));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll * intensity));
    }

    //
    public static void tickJump() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            resetJump();
            return;
        }
        prevJumpY = jumpY;
        prevJumpPitch = jumpPitch;
        Player player = mc.player;
        Vec3 vel = player.getDeltaMovement();
        boolean onGround = player.onGround();

        //fly！！！
        if (wasOnGround && !onGround && vel.y > 0.05) {
            //上下移动强度

            jumpYVel += 0.0025F;
            jumpPitchVel -= 0.3F;
        }
        //landing
        if (!wasOnGround && onGround && prevVerticalVel < -0.05F) {
            //落地强度
            float impact = Mth.clamp(-prevVerticalVel * 1.5F, 0.0F, 1.0F);
            //0.035f 1.4F系数越小落地越软 或者说落地上下摇晃程度
            jumpYVel -= 0.025F + impact * 0.035F;
            jumpPitchVel += 0.8F + impact * 1.4F;
        }

        // 竖直
        jumpYVel += -jumpY * 0.16F;
        jumpYVel *= 0.78F;
        jumpY += jumpYVel;

        //旋转
        jumpPitchVel += -jumpPitch * 0.18F;
        jumpPitchVel *= 0.76F;
        jumpPitch += jumpPitchVel;

        jumpY = Mth.clamp(jumpY, -0.08F, 0.08F);

        jumpPitch = Mth.clamp(jumpPitch, -5.0F, 0.65F);

        prevVerticalVel = (float) vel.y;
        wasOnGround = onGround;
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
        if (mc.player == null) {
            return;
        }

        float y = Mth.lerp(partialTick, prevJumpY, jumpY);

        float pitch = Mth.lerp(partialTick, prevJumpPitch, jumpPitch);

        float intensity = mc.player.isUsingItem() ? 0.15F : 1.0F;

        poseStack.translate(0.0F, y * intensity, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch * intensity));
    }

    /*
    下蹲摇晃
     */
    public static void applyCrouchMotion(PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Player player = mc.player;
        float target = player.isCrouching() ? 1.0F : 0.0F;
        crouchAmount = Mth.lerp(0.18F, crouchAmount, target);

        if (crouchAmount < 0.001F) {
            crouchAmount = 0.0F;
            return;
        }
        float time = player.tickCount + partialTick;
        float movement = Mth.clamp(player.walkAnimation.speed(partialTick) * 1.25F, 0.0F, 1.0F);
        float intensity = crouchAmount * (0.35F + movement * 0.65F);

        float x = Mth.sin(time * 0.2F) * 0.0025F * intensity;
        float y = Mth.cos(time * 0.4F) * 0.0015F * intensity;
        float pitch = Mth.cos(time * 0.2F) * 0.55F * intensity;
        float roll = Mth.sin(time * 0.2F) * 1.15F * intensity;

        float kickPitch = Mth.lerp(partialTick, prevCrouchKickPitch, crouchKickPitch);
        float kickRoll = Mth.lerp(partialTick, prevCrouchKickRoll, crouchKickRoll);

//        System.out.println(kickPitch);
//        System.out.println(kickRoll);

        poseStack.translate(x, y, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
        poseStack.mulPose(Axis.XP.rotationDegrees(kickPitch));
        poseStack.mulPose(Axis.ZP.rotationDegrees(kickRoll));
    }

    public static void tickCrouch() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            resetCrouch();
            return;
        }

        prevCrouchKickPitch = crouchKickPitch;
        prevCrouchKickRoll = crouchKickRoll;

        boolean crouching = mc.player.isCrouching();
        if (!wasCrouching && crouching) {
            crouchKickPitchVel -= 0.75F;
            crouchKickRollVel += 0.30F;
        }

        crouchKickPitchVel += -crouchKickPitch * 0.24F;
        crouchKickPitchVel *= 0.70F;
        crouchKickPitch += crouchKickPitchVel;

        crouchKickRollVel += -crouchKickRoll * 0.24F;
        crouchKickRollVel *= 0.70F;
        crouchKickRoll += crouchKickRollVel;

        crouchKickPitch = Mth.clamp(crouchKickPitch, -2.0F, 2.0F);
        crouchKickRoll = Mth.clamp(crouchKickRoll, -1.0F, 1.0F);

        wasCrouching = crouching;
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

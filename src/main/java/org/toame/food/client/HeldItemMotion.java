package org.toame.food.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import static org.toame.food.events.InputEvents.isAnimationLocked;

public class HeldItemMotion {
    /*
    idleMotion       自然晃动
    walkMotion       走路晃动
    inertiaMotion    惯性晃动
    jumpMotion       跳跃晃动
     */
    private static Vec3 lastHorizontalVelocity = Vec3.ZERO;

    private static float inertiaPitch;
    private static float inertiaPitchVelocity;

    private static float inertiaRoll;
    private static float inertiaRollVelocity;

    private static boolean wasOnGround = true;
    private static float lastVerticalVelocity;

    private static float jumpY;
    private static float jumpYVelocity;

    private static float jumpPitch;
    private static float jumpPitchVelocity;

    private static float previousInertiaPitch;
    private static float previousInertiaRoll;
    private static float previousJumpY;
    private static float previousJumpPitch;

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
        if (isAnimationLocked()) {
            amount *= 0.15F;
        }
        //系数越大 效果越明显
        float x = Mth.sin(phase) * 0.003F * amount;
        float y = Mth.cos(phase * 2.0F) * 0.0025F * amount;

        float pitch = Mth.cos(phase * 2.0F) * 0.45F * amount;//弹性
        float roll = Mth.sin(phase) * 0.55F * amount;

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

        float pitch = Mth.lerp(partialTick, previousInertiaPitch, inertiaPitch);

        float roll = Mth.lerp(partialTick, previousInertiaRoll, inertiaRoll);

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
        previousInertiaPitch = inertiaPitch;
        previousInertiaRoll = inertiaRoll;
        Player player = mc.player;
        //get运动矢量
        Vec3 velocity = player.getDeltaMovement();

        //水平矢量
        Vec3 currentHorizontalVelocity = new Vec3(velocity.x, 0.0, velocity.z);

        //水平加速度
        Vec3 acceleration = currentHorizontalVelocity.subtract(lastHorizontalVelocity);

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
        inertiaPitchVelocity -= forwardAcceleration * 16.0F;
        //侧向移动会产生侧倾
        inertiaRollVelocity += sideAcceleration * 10.0F;

        //弹回到初始位置
        inertiaPitchVelocity += -inertiaPitch * 0.20F;
        inertiaPitchVelocity *= 0.72F;
        inertiaPitch += inertiaPitchVelocity;

        inertiaRollVelocity += -inertiaRoll * 0.20F;
        inertiaRollVelocity *= 0.72F;
        inertiaRoll += inertiaRollVelocity;

        inertiaPitch = Mth.clamp(inertiaPitch, -8.0F, 8.0F);

        inertiaRoll = Mth.clamp(inertiaRoll, -6.0F, 6.0F);
        lastHorizontalVelocity = currentHorizontalVelocity;
    }

    public static void resetInertia() {
        lastHorizontalVelocity = Vec3.ZERO;

        inertiaPitch = 0.0F;
        inertiaPitchVelocity = 0.0F;

        inertiaRoll = 0.0F;
        inertiaRollVelocity = 0.0F;
    }
    //
    public static void tickJump() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) {
            resetJump();
            return;
        }
        previousJumpY = jumpY;
        previousJumpPitch = jumpPitch;
        Player player = mc.player;
        Vec3 velocity = player.getDeltaMovement();
        boolean onGround = player.onGround();

        //fly！！！
        if (wasOnGround && !onGround && velocity.y > 0.05) {
            jumpYVelocity += 0.035F;
            jumpPitchVelocity -= 1.0F;
        }
        //landing
        if (!wasOnGround && onGround && lastVerticalVelocity < -0.05F) {
            //落地强度
            float impact = Mth.clamp(-lastVerticalVelocity * 1.5F, 0.0F, 1.0F);
            //0.035f 1.4F系数越小落地越软
            jumpYVelocity -= 0.025F + impact * 0.035F;
            jumpPitchVelocity += 0.8F + impact * 1.4F;
        }

        // 竖直
        jumpYVelocity += -jumpY * 0.16F;
        jumpYVelocity *= 0.78F;
        jumpY += jumpYVelocity;

        //旋转
        jumpPitchVelocity += -jumpPitch * 0.18F;
        jumpPitchVelocity *= 0.76F;
        jumpPitch += jumpPitchVelocity;

        jumpY = Mth.clamp(jumpY, -0.08F, 0.08F);

        jumpPitch = Mth.clamp(jumpPitch, -5.0F, 5.0F);

        lastVerticalVelocity = (float) velocity.y;
        wasOnGround = onGround;
    }

    public static void resetJump() {
        wasOnGround = true;
        lastVerticalVelocity = 0.0F;

        jumpY = 0.0F;
        jumpYVelocity = 0.0F;

        jumpPitch = 0.0F;
        jumpPitchVelocity = 0.0F;
    }

    public static void applyJumpMotion(PoseStack poseStack, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        float y = Mth.lerp(partialTick, previousJumpY, jumpY);

        float pitch = Mth.lerp(partialTick, previousJumpPitch, jumpPitch);

        float intensity = mc.player.isUsingItem() ? 0.15F : 1.0F;

        poseStack.translate(0.0F, y * intensity, 0.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch * intensity));
    }
}

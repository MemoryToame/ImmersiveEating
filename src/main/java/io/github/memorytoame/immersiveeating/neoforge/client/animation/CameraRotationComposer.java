package io.github.memorytoame.immersiveeating.neoforge.client.animation;

import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.joml.Matrix3f;
import org.joml.Quaternionf;

/*
  将动画摄像机自身旋转应用
 */
public final class CameraRotationComposer {

    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0D);
    private static final float RAD_TO_DEG = (float) (180.0D / Math.PI);
    private static final float GIMBAL_LOCK_EPSILON = 1.745E-3F;
    private static final float MAX_EVENT_PITCH_RADIANS = 89.9F * DEG_TO_RAD;

    private static float blockbenchX;
    private static float blockbenchY;
    private static float blockbenchZ;
    private static boolean hasBlockbenchRotation;

    private CameraRotationComposer() {}

    public static void applyBlockbenchRotation(ViewportEvent.ComputeCameraAngles event, float xDegrees, float yDegrees, float zDegrees) {
        applyLocalCameraRotation(event, xDegrees, yDegrees, zDegrees);
    }

    public static void captureBlockbenchRotationRadians(float xRadians, float yRadians, float zRadians) {
        blockbenchX = xRadians;
        blockbenchY = yRadians;
        blockbenchZ = zRadians;
        hasBlockbenchRotation = true;
    }

    public static void applyCapturedBlockbenchRotation(ViewportEvent.ComputeCameraAngles event) {
        if (!hasBlockbenchRotation) {
            return;
        }
        applyBlockbenchRotation(event, blockbenchX * RAD_TO_DEG, blockbenchY * RAD_TO_DEG, blockbenchZ * RAD_TO_DEG);
    }
    public static void clearBlockbenchRotation() {
        blockbenchX = 0.0F;
        blockbenchY = 0.0F;
        blockbenchZ = 0.0F;
        hasBlockbenchRotation = false;
    }


    public static void applyForgeOffset(ViewportEvent.ComputeCameraAngles event, float pitchDegrees, float yawDegrees, float rollDegrees) {
        applyLocalCameraRotation(event, -pitchDegrees, -yawDegrees, -rollDegrees);
    }

    private static void applyLocalCameraRotation(ViewportEvent.ComputeCameraAngles event, float xDegrees, float yDegrees, float zDegrees) {
        Quaternionf baseViewRotation = new Quaternionf()
                .rotationZ(event.getRoll() * DEG_TO_RAD)
                .rotateX(event.getPitch() * DEG_TO_RAD)
                .rotateY((event.getYaw() + 180.0F) * DEG_TO_RAD);

        //相机局部旋转
        Quaternionf localCameraRotation = new Quaternionf()
                .rotationX(xDegrees * DEG_TO_RAD)
                .rotateY(yDegrees * DEG_TO_RAD)
                .rotateZ(zDegrees * DEG_TO_RAD);

        //逆相机矩阵
        Quaternionf finalViewRotation = localCameraRotation.conjugate().mul(baseViewRotation);
        Matrix3f matrix = new Matrix3f().rotation(finalViewRotation);

        float pitchRadians = (float) Math.asin(Mth.clamp(matrix.m12(), -1.0F, 1.0F));
        float eventPitchRadians = Mth.clamp(
                pitchRadians,
                -MAX_EVENT_PITCH_RADIANS,
                MAX_EVENT_PITCH_RADIANS
        );
        float cosPitch = (float) Math.cos(pitchRadians);
        float rollRadians;
        float yawWithOffsetRadians;

        if (Math.abs(cosPitch) > GIMBAL_LOCK_EPSILON) {
            
            rollRadians = (float) Math.atan2(-matrix.m10(), matrix.m11());
            yawWithOffsetRadians = (float) Math.atan2(-matrix.m02(), matrix.m22());

        } else {
            //欧拉角
            yawWithOffsetRadians = (event.getYaw() + 180.0F) * DEG_TO_RAD;
            float coupledAngle = (float) Math.atan2(matrix.m01(), matrix.m00());
            rollRadians = pitchRadians >= 0.0F ? coupledAngle - yawWithOffsetRadians : coupledAngle + yawWithOffsetRadians;
        }

        event.setRoll(rollRadians * RAD_TO_DEG);
        event.setPitch(eventPitchRadians * RAD_TO_DEG);
        event.setYaw(Mth.wrapDegrees(yawWithOffsetRadians * RAD_TO_DEG - 180.0F));
    }
}

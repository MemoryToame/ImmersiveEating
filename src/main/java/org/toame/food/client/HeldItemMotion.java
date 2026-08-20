package org.toame.food.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class HeldItemMotion {
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

}

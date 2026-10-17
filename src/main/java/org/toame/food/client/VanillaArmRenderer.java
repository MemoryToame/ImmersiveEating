package org.toame.food.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;

public final class VanillaArmRenderer {

    private static final float PIXEL = 1.0F / 16.0F;

    public record CapturedArm(HumanoidArm arm, Matrix4f pose, Matrix3f normal) { }

    public static boolean isArmBone(GeoBone bone) {
        if (isDedicatedAnchor(bone)) {
            return true;
        }
        return getArm(bone) != null && !hasDedicatedAnchorChild(bone);
    }

    public static CapturedArm captureArmPose(PoseStack poseStack, GeoBone bone, boolean mirrorModel) {
        HumanoidArm arm = getArm(bone);
        GeoCube armCube = findArmCube(bone);

        if (arm == null) {
            return null;
        }

        poseStack.pushPose();
        if (armCube == null) {
            poseStack.popPose();
            return null;
        }

        HumanoidArm renderedArm = mirrorModel ? opposite(arm) : arm;
        Matrix4f armPose = createPreviewCubeArmPose(poseStack, renderedArm, armCube, ArmModelOffset.forPlayer(Minecraft.getInstance().player).matrixFor(renderedArm));
        CapturedArm capturedArm = new CapturedArm(renderedArm, armPose, new Matrix3f(armPose).normal());
        poseStack.popPose();
        return capturedArm;
    }

    private static HumanoidArm opposite(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public static void renderCapturedArm(CapturedArm capturedArm, MultiBufferSource bufferSource, int packedLight,
                                         float partialTick, boolean mirrored) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.player.isInvisible()) {
            return;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().set(capturedArm.pose());
        poseStack.last().normal().set(capturedArm.normal());
        HeldItemMotion.applyArmInertiaMotion(poseStack, partialTick);
        MultiBufferSource armBuffer = mirrored ? new MirroredArmBufferSource(bufferSource, minecraft.player.getSkinTextureLocation()) : bufferSource;
        renderVanillaArm(minecraft, capturedArm, poseStack, armBuffer, packedLight);
    }
    private static void renderVanillaArm(Minecraft minecraft, CapturedArm capturedArm, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        PlayerRenderer playerRenderer = (PlayerRenderer) minecraft.getEntityRenderDispatcher().getRenderer(minecraft.player);
        if (capturedArm.arm() == HumanoidArm.RIGHT) {
            playerRenderer.renderRightHand(poseStack, bufferSource, packedLight, minecraft.player);
        } else {
            playerRenderer.renderLeftHand(poseStack, bufferSource, packedLight, minecraft.player);
        }
    }

    public static void renderCapturedArms(Iterable<CapturedArm> capturedArms, MultiBufferSource bufferSource, int packedLight,
                                          float partialTick, boolean mirrored) {
        for (CapturedArm capturedArm : capturedArms) {
            renderCapturedArm(capturedArm, bufferSource, packedLight, partialTick, mirrored);
        }
    }

    private record MirroredArmBufferSource(MultiBufferSource delegate, net.minecraft.resources.ResourceLocation skin)
            implements MultiBufferSource {
        @Override
        public com.mojang.blaze3d.vertex.VertexConsumer getBuffer(RenderType renderType) {
            if (renderType == RenderType.entitySolid(skin)) {
                return delegate.getBuffer(RenderType.entityTranslucent(skin));
            }
            return delegate.getBuffer(renderType);
        }
    }

    private static GeoCube findArmCube(GeoBone bone) {
        return bone.getCubes().isEmpty() ? null : bone.getCubes().get(0);
    }
    private static HumanoidArm getArm(GeoBone bone) {
        String normalizedName = normalize(bone.getName());
        return switch (normalizedName) {
            case "rightarm", "righthandpos" -> HumanoidArm.RIGHT;
            case "leftarm", "lefthandpos" -> HumanoidArm.LEFT;
            default -> null;
        };
    }

    private static boolean isDedicatedAnchor(GeoBone bone) {
        String normalizedName = normalize(bone.getName());
        return normalizedName.equals("lefthandpos") || normalizedName.equals("righthandpos");
    }

    private static boolean hasDedicatedAnchorChild(GeoBone bone) {
        for (GeoBone child : bone.getChildBones()) {
            if (isDedicatedAnchor(child)) {
                return true;
            }
        }

        return false;
    }

    private static String normalize(String name) {
        return name.replace("_", "").replace(" ", "").toLowerCase();
    }

    private static Matrix4f createPreviewCubeArmPose(PoseStack poseStack, HumanoidArm arm, GeoCube cube, Matrix4f localOffset) {
        Bounds bounds = findBounds(cube);
        Vector3f previewCenter = new Vector3f(
                (bounds.minX() + bounds.maxX()) * 0.5F,
                (bounds.minY() + bounds.maxY()) * 0.5F,
                (bounds.minZ() + bounds.maxZ()) * 0.5F
        );

        Matrix4f cubeTransform = new Matrix4f().identity();
        Vec3 cubePivot = cube.pivot();
        Vec3 cubeRotation = cube.rotation();
        cubeTransform.translate(
                (float) cubePivot.x() / 16.0F,
                (float) cubePivot.y() / 16.0F,
                (float) cubePivot.z() / 16.0F
        );
        cubeTransform.rotateZ((float) cubeRotation.z());
        cubeTransform.rotateY((float) cubeRotation.y());
        cubeTransform.rotateX((float) cubeRotation.x());
        cubeTransform.translate(
                -(float) cubePivot.x() / 16.0F,
                -(float) cubePivot.y() / 16.0F,
                -(float) cubePivot.z() / 16.0F
        );
        Vector3f targetCenter = cubeTransform.transformPosition(previewCenter);
        Vector3f vanillaCenter = arm == HumanoidArm.RIGHT ? new Vector3f(-6.0F, 6.0F, 0.0F).mul(PIXEL) :
                new Vector3f(6.0F, 6.0F, 0.0F).mul(PIXEL);
        Matrix4f vanillaToPreview = new Matrix4f().identity();
        vanillaToPreview.translate(targetCenter);
        vanillaToPreview.rotateZ((float) cubeRotation.z());
        vanillaToPreview.rotateY((float) cubeRotation.y());
        vanillaToPreview.rotateX((float) cubeRotation.x());
        vanillaToPreview.rotateZ((float) Math.PI);
        Vec3 armSize = cube.size();
        vanillaToPreview.scale(
                safeScale((float) armSize.x(), 4.0F),
                safeScale((float) armSize.y(), 12.0F),
                safeScale((float) armSize.z(), 4.0F)
        );
        vanillaToPreview.translate(-vanillaCenter.x(), -vanillaCenter.y(), -vanillaCenter.z());
        return new Matrix4f(poseStack.last().pose()).mul(localOffset).mul(vanillaToPreview);
    }
    private static float safeScale(float size, float vanillaSize) {
        return size > 0.0F ? size / vanillaSize : 1.0F;
    }
    private static Bounds findBounds(GeoCube cube) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (GeoQuad quad : cube.quads()) {
            if (quad == null) {
                continue;
            }

            for (GeoVertex vertex : quad.vertices()) {
                Vector3f position = vertex.position();
                minX = Math.min(minX, position.x());
                minY = Math.min(minY, position.y());
                minZ = Math.min(minZ, position.z());
                maxX = Math.max(maxX, position.x());
                maxY = Math.max(maxY, position.y());
                maxZ = Math.max(maxZ, position.z());
            }
        }

        if (Float.isInfinite(minX)) {
            return new Bounds(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        }

        return new Bounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private record Bounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
    }
}

package org.toame.food.additions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix3f;
import org.toame.food.Food;
import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.client.CameraRotationComposer;
import org.toame.food.client.ClientAnimationManager;
import org.toame.food.client.HeldItemMotion;
import org.toame.food.client.VanillaArmRenderer;
import org.toame.food.additions.Empty;
import org.toame.food.init.ModItems;
import org.toame.food.utils.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomRenderer extends GeoItemRenderer<Empty> {


    public static Map<String, CustomRenderer> rendererMap = new HashMap<>();
    private final List<VanillaArmRenderer.CapturedArm> capturedArms = new ArrayList<>();
    private final String itemId;
    private boolean mirrorLeftHandModel;
    private long renderInstanceId = Long.MAX_VALUE;

    public CustomRenderer(String id) {
        super(new Model(getModelId(id)));
        this.itemId = getItemId(id);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, Empty animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        RenderUtils.prepMatrixForBone(poseStack, bone);
        if (mirrorLeftHandModel && bone.getParent() == null) {
            Matrix4f reflection = new Matrix4f().m00(-1.0F);
            poseStack.last().pose().mul(reflection);
            poseStack.last().normal().mul(new Matrix3f(reflection));
        }
        boolean cameraBone = "camera".equals(bone.getName());
        boolean armBone = VanillaArmRenderer.isArmBone(bone);
        boolean moveBoneBehindCamera = !hasInvisibleBoneParent(bone)
                && isInvisibleBone(bone)
                && shouldHideInvisibleBone(partialTick);

        if (cameraBone) {
            CameraRotationComposer.captureBlockbenchRotationRadians(bone.getRotX(), bone.getRotY(), bone.getRotZ());
        }

        if (moveBoneBehindCamera) {
            poseStack.translate(0.0F, 0.0F, -1000.0F);
        }

        if (armBone) {
            if (!isReRender) {
                VanillaArmRenderer.CapturedArm capturedArm = VanillaArmRenderer.captureArmPose(
                        poseStack, bone, mirrorLeftHandModel);
                if (capturedArm != null) {
                    capturedArms.add(capturedArm);
                }
            }
        } else {
            renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay,red,blue,green,alpha);
        }
        if (!isReRender && !armBone) {
            applyRenderLayersForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }
        renderChildBones(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red,blue,green,alpha);
        poseStack.popPose();
    }
    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        capturedArms.clear();
        poseStack.pushPose();

        float partialTick = Minecraft.getInstance().getFrameTime();
        HeldItemMotion.applyIdleMotion(poseStack, partialTick);
        HeldItemMotion.applyWalkMotion(poseStack, partialTick);
        HeldItemMotion.applyInertiaMotion(poseStack,partialTick);
        HeldItemMotion.applyJumpMotion(poseStack,partialTick);
        HeldItemMotion.applyCrouchMotion(poseStack, partialTick);

        boolean firstPerson = transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        mirrorLeftHandModel = transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        boolean mirroredRender = mirrorLeftHandModel;
        if (firstPerson) {
            CameraRotationComposer.clearBlockbenchRotation();
        }
        try {
            super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            mirrorLeftHandModel = false;
        }
        poseStack.popPose();
        VanillaArmRenderer.renderCapturedArms(capturedArms, bufferSource, packedLight, partialTick, mirroredRender);
        capturedArms.clear();
    }

    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, long instanceId) {
        renderInstanceId = instanceId;
        try {
            renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
        } finally {
            renderInstanceId = Long.MAX_VALUE;
        }
    }

    @Override
    public long getInstanceId(Empty animatable) {
        return renderInstanceId != Long.MAX_VALUE ? renderInstanceId : super.getInstanceId(animatable);
    }

    @Override
    public void renderCube(PoseStack poseStack, GeoCube cube, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        RenderUtils.translateToPivotPoint(poseStack, cube);
        RenderUtils.rotateMatrixAroundCube(poseStack, cube);
        RenderUtils.translateAwayFromPivotPoint(poseStack, cube);

        Matrix3f normalMatrix = poseStack.last().normal();
        Matrix4f poseMatrix = new Matrix4f(poseStack.last().pose());
        for (GeoQuad quad : cube.quads()) {
            if (quad == null) {
                continue;
            }

            Vector3f normal = normalMatrix.transform(new Vector3f(quad.normal()));
            RenderUtils.fixInvertedFlatCube(cube, normal);
            GeoVertex[] vertices = quad.vertices();
            if (mirrorLeftHandModel) {
                for (int i = vertices.length - 1; i >= 0; i--) {
                    addMirroredVertex(vertices[i], poseMatrix, normal, buffer, packedLight, packedOverlay, red,blue,green,alpha);
                }
            } else {
                for (GeoVertex vertex : vertices) {
                    addMirroredVertex(vertex, poseMatrix, normal, buffer, packedLight, packedOverlay,  red,blue,green,alpha);
                }
            }
        }
    }
    private static void addMirroredVertex(GeoVertex vertex, Matrix4f poseMatrix, Vector3f normal, VertexConsumer buffer,
                                          int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        Vector4f position = poseMatrix.transform(new Vector4f(vertex.position().x(), vertex.position().y(), vertex.position().z(), 1.0F));
        buffer.vertex(position.x(), position.y(), position.z(), red,blue,green,alpha, vertex.texU(), vertex.texV(),
                packedOverlay, packedLight, normal.x(), normal.y(), normal.z());
    }

    @Override
    public RenderType getRenderType(Empty animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucentCull(texture);
    }

    private static String getModelId(String id) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        return resourceLocation == null ? id : resourceLocation.getPath();
    }

    private static String getItemId(String id) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        return resourceLocation == null ? id : resourceLocation.toString();
    }

    private boolean shouldHideInvisibleBone(float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        boolean armSwinging = mc.player != null
                && (mc.player.swinging || mc.player.getAttackAnim(partialTick) > 1.0E-4F);
        boolean animationPlaying = AnimationUtils.isAnimationLocked() && isPlayingAnimation();
        return armSwinging || !animationPlaying;
    }

    private boolean isInvisibleBone(GeoBone bone) {
        return FoodDefinitionManager.isInvisible(itemId, bone.getName());
    }

    private boolean hasInvisibleBoneParent(GeoBone bone) {
        for (GeoBone parent = bone.getParent(); parent != null; parent = parent.getParent()) {
            if (isInvisibleBone(parent)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPlayingAnimation() {
        ItemStack stack = getCurrentItemStack();
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        long instanceId = mc.player == null ? Long.MAX_VALUE : ClientAnimationManager.getOrCreateControllerId(mc.player);
        boolean result = false;
        for (AnimationController<?> controller3: ModItems.EMPTY.get().getAnimatableInstanceCache().getManagerForId(instanceId).getAnimationControllers().values()){
            if (controller3 != null && controller3.isPlayingTriggeredAnimation()
                    && "eat".equals(controller3.getName())) {
                result=true;
            }
        }
        return result;
    }

    private static class Model extends GeoModel<Empty> {
        private final String id;
        public Model(String id) {
            this.id = id;
        }

        @Override
        public ResourceLocation getModelResource(Empty animatable) {
            return new ResourceLocation(Food.MODID, "geo/" + id + ".geo.json");}

        @Override
        public ResourceLocation getTextureResource(Empty animatable) {
            return new ResourceLocation(Food.MODID, "textures/item/" + id + ".png");
        }

        @Override
    public ResourceLocation getAnimationResource(Empty animatable) {
            return new ResourceLocation(
                    Food.MODID,
                    "animations/" + id + ".animation.json"
            );
        }
    }
}

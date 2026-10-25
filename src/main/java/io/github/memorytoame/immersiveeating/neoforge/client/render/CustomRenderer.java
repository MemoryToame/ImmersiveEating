package io.github.memorytoame.immersiveeating.neoforge.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.client.animation.CameraRotationComposer;
import io.github.memorytoame.immersiveeating.neoforge.client.animation.ClientAnimationManager;
import io.github.memorytoame.immersiveeating.neoforge.client.animation.HeldItemMotion;
import io.github.memorytoame.immersiveeating.neoforge.client.animation.VanillaArmRenderer;
import io.github.memorytoame.immersiveeating.neoforge.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.common.content.item.Empty;
import io.github.memorytoame.immersiveeating.neoforge.init.ModItems;
import io.github.memorytoame.immersiveeating.neoforge.utils.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.object.GeoVertex;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomRenderer extends GeoItemRenderer<Empty> {

    public static Map<String, CustomRenderer> rendererMap = new HashMap<>();
    private final List<VanillaArmRenderer.CapturedArm> capturedArms = new ArrayList<>();
    private final String itemId;
    private boolean mirrorLeftHandModel;

    public CustomRenderer(String id) {
        super(new Model(getModelId(id)));
        this.itemId = getItemId(id);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, Empty animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        poseStack.pushPose();
        RenderUtil.prepMatrixForBone(poseStack, bone);
        if (mirrorLeftHandModel && bone.getParent() == null) {
            poseStack.mulPose(new Matrix4f().m00(-1.0F));
        }
        boolean cameraBone = "camera".equals(bone.getName());
        boolean armBone = VanillaArmRenderer.isArmBone(bone);
        boolean moveBoneBehindCamera = !hasInvisibleBoneParent(bone) && isInvisibleBone(bone) && shouldHideInvisibleBone(partialTick);

        if (cameraBone) {
            CameraRotationComposer.captureBlockbenchRotationRadians(bone.getRotX(), bone.getRotY(), bone.getRotZ());
        }

        if (moveBoneBehindCamera) {
            poseStack.translate(0.0F, 0.0F, -1000.0F);
        }

        if (armBone) {
            if (!isReRender) {
                VanillaArmRenderer.CapturedArm capturedArm = VanillaArmRenderer.captureArmPose(poseStack, bone, mirrorLeftHandModel);
                if (capturedArm != null) {
                    capturedArms.add(capturedArm);
                }
            }
        } else {
            renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay,colour);
        }
        if (!isReRender) {
            applyRenderLayersForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }
        renderChildBones(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        poseStack.popPose();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        capturedArms.clear();
        poseStack.pushPose();

        float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        HeldItemMotion.applyIdleMotion(poseStack, partialTick);
        HeldItemMotion.applyWalkMotion(poseStack, partialTick);
        HeldItemMotion.applyInertiaMotion(poseStack,partialTick);
        HeldItemMotion.applyJumpMotion(poseStack,partialTick);
        HeldItemMotion.applyCrouchMotion(poseStack, partialTick);

        boolean firstPerson = transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
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

    @Override
    public void renderCube(PoseStack poseStack, GeoCube cube, VertexConsumer buffer, int packedLight, int packedOverlay, int colour) {
        RenderUtil.translateToPivotPoint(poseStack, cube);
        RenderUtil.rotateMatrixAroundCube(poseStack, cube);
        RenderUtil.translateAwayFromPivotPoint(poseStack, cube);

        Matrix3f normalMatrix = poseStack.last().normal();
        Matrix4f poseMatrix = new Matrix4f(poseStack.last().pose());
        for (GeoQuad quad : cube.quads()) {
            if (quad == null) {
                continue;
            }

            Vector3f normal = normalMatrix.transform(new Vector3f(quad.normal()));
            RenderUtil.fixInvertedFlatCube(cube, normal);
            GeoVertex[] vertices = quad.vertices();
            if (mirrorLeftHandModel) {
                for (int i = vertices.length - 1; i >= 0; i--) {
                    addMirroredVertex(vertices[i], poseMatrix, normal, buffer, packedLight, packedOverlay, colour);
                }
            } else {
                for (GeoVertex vertex : vertices) {
                    addMirroredVertex(vertex, poseMatrix, normal, buffer, packedLight, packedOverlay, colour);
                }
            }
        }
    }

    private static void addMirroredVertex(GeoVertex vertex, Matrix4f poseMatrix, Vector3f normal, VertexConsumer buffer,
                                          int packedLight, int packedOverlay, int colour) {
        Vector4f position = poseMatrix.transform(new Vector4f(vertex.position().x(), vertex.position().y(), vertex.position().z(), 1.0F));
        buffer.addVertex(position.x(), position.y(), position.z(), colour, vertex.texU(), vertex.texV(),
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
        long instanceId = GeoItem.getId(stack);
        if (instanceId == Long.MAX_VALUE && Minecraft.getInstance().player != null) {
            instanceId = ClientAnimationManager.getOrCreateControllerId(Minecraft.getInstance().player);
        }
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
            return ResourceLocation.fromNamespaceAndPath(
                    Food.MODID,
                    "geo/" + id + ".geo.json"
            );
        }

        @Override
        public ResourceLocation getTextureResource(Empty animatable) {
            return ResourceLocation.fromNamespaceAndPath(
                    Food.MODID,
                    "textures/item/" + id + ".png"
            );
        }

        @Override
        public ResourceLocation getAnimationResource(Empty animatable) {
            return ResourceLocation.fromNamespaceAndPath(
                    Food.MODID,
                    "animations/" + id + ".animation.json"
            );
        }
    }
}

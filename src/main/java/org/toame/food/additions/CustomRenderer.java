package org.toame.food.additions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.toame.food.Food;
import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.client.HeldItemMotion;
import org.toame.food.client.VanillaArmRenderer;
import org.toame.food.init.ModItems;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomRenderer extends GeoItemRenderer<Empty> {

    public static Map<String, CustomRenderer> rendererMap = new HashMap<>();
    public static List<String> init_ItemIdList = new ArrayList<>();
    public static List<Item> init_ItemList = new ArrayList<>();
    private final List<VanillaArmRenderer.CapturedArm> capturedArms = new ArrayList<>();
    private final String itemId;

    public CustomRenderer(String id) {
        super(new Model(getModelId(id)));
        this.itemId = getItemId(id);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, Empty animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.pushPose();
        RenderUtils.prepMatrixForBone(poseStack, bone);
        boolean armBone = VanillaArmRenderer.isArmBone(bone);
        boolean hiddenInModelStage = isHiddenInModelStage(bone);

        if (armBone && !hiddenInModelStage) {
            if (!isReRender) {
                VanillaArmRenderer.CapturedArm capturedArm = VanillaArmRenderer.captureArmPose(poseStack, bone);
                if (capturedArm != null) {
                    capturedArms.add(capturedArm);
                }
            }
        } else if (!hiddenInModelStage) {
            renderCubesOfBone(poseStack, bone, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        if (!isReRender && !armBone && !hiddenInModelStage) {
            applyRenderLayersForBone(poseStack, animatable, bone, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }
        renderChildBones(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.popPose();
    }

    @Override
    public void renderByItem(ItemStack stack, net.minecraft.world.item.ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        capturedArms.clear();
        poseStack.pushPose();

        float partialTick = Minecraft.getInstance().getFrameTime();
        HeldItemMotion.applyIdleMotion(poseStack, partialTick);
        HeldItemMotion.applyWalkMotion(poseStack, partialTick);
        HeldItemMotion.applyInertiaMotion(poseStack,partialTick);
        HeldItemMotion.applyJumpMotion(poseStack,partialTick);

        super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.popPose();
        VanillaArmRenderer.renderCapturedArms(capturedArms, bufferSource, packedLight);
        capturedArms.clear();
    }

    @Override
    public RenderType getRenderType(Empty animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucentCull(texture);
    }

    public ItemStack getItemStack() {
        return ModItems.EMPTY.get().getRenderStack();
    }

    private static String getModelId(String id) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        return resourceLocation == null ? id : resourceLocation.getPath();
    }

    private static String getItemId(String id) {
        ResourceLocation resourceLocation = ResourceLocation.tryParse(id);
        return resourceLocation == null ? id : resourceLocation.toString();
    }

    private boolean isHiddenInModelStage(GeoBone bone) {
        return !isPlayingAnimation() && isInvisibleBoneOrParent(bone);
    }

    private boolean isInvisibleBoneOrParent(GeoBone bone) {
        for (GeoBone current = bone; current != null; current = current.getParent()) {
            if (FoodDefinitionManager.isInvisible(itemId, current.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isPlayingAnimation() {
        long instanceId = GeoItem.getId(getItemStack());
        AnimationController<?> controller = ModItems.EMPTY.get().getAnimatableInstanceCache()
                .getManagerForId(instanceId)
                .getAnimationControllers()
                .get("eat");
        return controller != null && controller.isPlayingTriggeredAnimation();
    }

    private static class Model extends GeoModel<Empty> {
        private final String id;

        public Model(String id) {
            this.id = id;
        }

        @Override
        public ResourceLocation getModelResource(Empty animatable) {
            return new ResourceLocation(
                    Food.MODID,
                    "geo/" + id + ".geo.json"
            );
        }

        @Override
        public ResourceLocation getTextureResource(Empty animatable) {
            return new ResourceLocation(
                    Food.MODID,
                    "textures/item/" + id + ".png"
            );
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

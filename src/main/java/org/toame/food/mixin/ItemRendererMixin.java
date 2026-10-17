package org.toame.food.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import org.toame.food.additions.CustomRenderer;
import org.toame.food.client.TransformationMatrixProperties;
import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.client.ClientAnimationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.toame.food.init.ModItems;

import static org.toame.food.client.TransformationMatrixProperties.NORMAL;
import static org.toame.food.events.InputEvents.*;
import static org.toame.food.additions.CustomRenderer.rendererMap;


@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", at = @At("HEAD"), cancellable = true)
    private void renderStatic(LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, Level level, int light, int overlay, int seed, CallbackInfo ci){
        Minecraft mc = Minecraft.getInstance();
        boolean leftMainHand = mc.player != null && mc.player.getMainArm() == HumanoidArm.LEFT;
        ItemDisplayContext mainHandContext = leftMainHand ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        if (context != mainHandContext) {
            return;
        }
        if (mc.player != null && mc.player.isUsingItem() && ItemStack.isSameItem(mc.player.getUseItem(), stack)) {
            return;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (FoodDefinitionManager.init_ItemIdList.contains(id.toString())){
            poseStack.pushPose();
            //xyz加 分别对应 右 上 后
            if (leftMainHand) {
                poseStack.translate(-1.00F - NORMAL.getTranslateX(), NORMAL.getTranslateY(), NORMAL.getTranslateZ());
            } else {
                poseStack.translate(NORMAL.getTranslateX(), NORMAL.getTranslateY(), NORMAL.getTranslateZ());
            }
            // poseStack.translate(debugX, debugY, debugZ);
            CustomRenderer customRenderer = CustomRenderer.rendererMap.get(id.getPath());
            if (customRenderer == null) {
                customRenderer = new CustomRenderer(id.toString());//id.toString 因为构造器里有一个解析path的方法
                CustomRenderer.rendererMap.put(id.getPath(), customRenderer);
            }
            ItemStack proxy = new ItemStack(ModItems.EMPTY.get());
            long instanceId = mc.player == null ? Long.MAX_VALUE : ClientAnimationManager.getOrCreateControllerId(mc.player);
            customRenderer.renderByItem(proxy, context, poseStack, buffer, light, overlay, instanceId);
            poseStack.popPose();
            ci.cancel();
        }
    }
}

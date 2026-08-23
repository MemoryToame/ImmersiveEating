package io.github.memorytoame.immersiveeating.neoforge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.memorytoame.immersiveeating.neoforge.additions.CustomRenderer;
import io.github.memorytoame.immersiveeating.neoforge.additions.definiton.FoodDefinitionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.memorytoame.immersiveeating.neoforge.client.TransformationMatrixProperties.NORMAL;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", at = @At("HEAD"), cancellable = true)
    private void renderStatic(LivingEntity entity, ItemStack stack, ItemDisplayContext context, boolean leftHand, PoseStack poseStack, MultiBufferSource buffer, Level level, int light, int overlay, int seed, CallbackInfo ci){
        if (context != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.isUsingItem() && ItemStack.isSameItem(mc.player.getUseItem(), stack)) {
            return;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) {
            return;
        }
        if (FoodDefinitionManager.init_ItemIdList.contains(id.toString())){
            poseStack.pushPose();
            // Translate on X, Y, Z respectively.
            //xyz加 分别对应 右 上 后
            poseStack.translate(NORMAL.getTranslateX(), NORMAL.getTranslateY(), NORMAL.getTranslateZ());
            // poseStack.translate(debugX, debugY, debugZ);
            CustomRenderer customRenderer = CustomRenderer.rendererMap.get(id.getPath());
            if (customRenderer == null) {
                customRenderer = new CustomRenderer(id.toString());//id.toString 因为构造器里有一个解析path的方法
                CustomRenderer.rendererMap.put(id.getPath(), customRenderer);
            }
            customRenderer.renderByItem(customRenderer.getItemStack(), context, poseStack, buffer, light, overlay);
            poseStack.popPose();
            ci.cancel();
        }
    }
}

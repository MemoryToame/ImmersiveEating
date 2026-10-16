package io.github.memorytoame.immersiveeating.neoforge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.memorytoame.immersiveeating.neoforge.client.render.CustomRenderer;
import io.github.memorytoame.immersiveeating.neoforge.client.animation.ClientAnimationManager;
import io.github.memorytoame.immersiveeating.neoforge.client.render.TransformationMatrixProperties;
import io.github.memorytoame.immersiveeating.neoforge.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.init.ModItems;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.GeckoLibConstants;

import static io.github.memorytoame.immersiveeating.neoforge.client.render.TransformationMatrixProperties.NORMAL;


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
            long instanceId = GeoItem.getId(stack);
            if (instanceId == Long.MAX_VALUE && mc.player != null) {
                instanceId = ClientAnimationManager.getOrCreateControllerId(mc.player);
            }
            if (instanceId != Long.MAX_VALUE) {
                proxy.set(GeckoLibConstants.STACK_ANIMATABLE_ID_COMPONENT.get(), instanceId);
            }
            customRenderer.renderByItem(proxy, context, poseStack, buffer, light, overlay);
            poseStack.popPose();
            ci.cancel();
        }
    }
}

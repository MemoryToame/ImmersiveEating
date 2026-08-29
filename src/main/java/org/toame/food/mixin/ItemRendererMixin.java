package org.toame.food.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.toame.food.additions.CustomRenderer;
import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.init.ModItems;
import software.bernie.geckolib.animatable.GeoItem;

import static org.toame.food.additions.CustomRenderer.rendererMap;
import static org.toame.food.client.TransformationMatrixProperties.NORMAL;

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
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) {
            return;
        }
        if (FoodDefinitionManager.init_ItemIdList.contains(id.toString())){
            poseStack.pushPose();
            //xyz加 分别对应 右 上 后
            poseStack.translate(NORMAL.getTranslateX(), NORMAL.getTranslateY(), NORMAL.getTranslateZ());
            //poseStack.translate(debugX, debugY, debugZ);
            CustomRenderer customRenderer = rendererMap.get(id.getPath());
            if (customRenderer == null) {
                customRenderer = new CustomRenderer(id.toString()); //id.toString 因为构造器里有一个解析path的方法
                rendererMap.put(id.getPath(), customRenderer);
            }
            ItemStack proxy = new ItemStack(ModItems.EMPTY.get());
            long instanceId = GeoItem.getId(stack);
            if (instanceId != Long.MAX_VALUE) {
                proxy.getOrCreateTag().putLong(GeoItem.ID_NBT_KEY, instanceId);
            }
            customRenderer.renderByItem(proxy, context, poseStack, buffer, light, overlay);
            poseStack.popPose();
            ci.cancel();
        }
    }
}

package io.github.memorytoame.immersiveeating.neoforge.mixin;

import io.github.memorytoame.immersiveeating.neoforge.client.animation.AnimationResourceChecker;
import io.github.memorytoame.immersiveeating.neoforge.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.common.content.item.Empty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow
    public abstract void itemUsed(InteractionHand hand);

    @Shadow
    private ItemStack mainHandItem;

    @Shadow
    private float mainHandHeight;

    @Shadow
    private float oMainHandHeight;

    @Unique
    private ItemStack food$selectedMainHandItem = ItemStack.EMPTY;
    //主手上物品
    @Unique
    private ItemStack food$renderedMainHandItem = ItemStack.EMPTY;
    //等待切换的物品
    @Unique
    private ItemStack food$pendingMainHandItem = ItemStack.EMPTY;
    //旧物品
    @Unique
    private ItemStack food$switchOutItem = ItemStack.EMPTY;
    @Unique
    private boolean food$hasTrackedSelectedItem;
    @Unique
    private boolean food$hasTrackedRenderedItem;
    @Unique
    private boolean food$waitingForSwitchOut;
    @Unique
    private boolean food$hasTrackedCustomMainHand;
    @Unique
    private boolean food$customMainHand;

    @Inject(method = "tick", at = @At("HEAD"))
    private void food$prepareOffHandEquipAnimation(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            food$hasTrackedCustomMainHand = false;
            food$customMainHand = false;
            return;
        }

        boolean customMainHand =FoodDefinitionManager.init_ItemList
                .contains(mc.player.getMainHandItem().getItem());
        if (food$hasTrackedCustomMainHand && food$customMainHand && !customMainHand) {
            itemUsed(InteractionHand.OFF_HAND);
        }
        food$customMainHand = customMainHand;
        food$hasTrackedCustomMainHand = true;
    }

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void food$hideOffHandForCustomMainHand(
            AbstractClientPlayer player,
            float partialTick,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack stack,
            float equipProgress,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci
    ) {
        if (hand == InteractionHand.OFF_HAND && food$isCustomMainHandBeingRendered(player)) {
            ci.cancel();
        }
    }

    @Unique
    private boolean food$isCustomMainHandBeingRendered(AbstractClientPlayer player) {
        if (food$waitingForSwitchOut) {
            return true;
        }
        if (FoodDefinitionManager.init_ItemList
                .contains(player.getMainHandItem().getItem())) {
            return true;
        }
        return !food$renderedMainHandItem.isEmpty() && FoodDefinitionManager.init_ItemList.contains(food$renderedMainHandItem.getItem());
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void food$triggerSwitchOut(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            food$resetTracking();
            return;
        }

        ItemStack mainHandItem = mc.player.getMainHandItem();

        //记录物品
        if (!food$hasTrackedSelectedItem) {
            food$selectedMainHandItem = mainHandItem.copy();
            food$hasTrackedSelectedItem = true;
            return;
        }

        if (!ItemStack.isSameItemSameComponents(food$selectedMainHandItem, mainHandItem)) {
            food$selectedMainHandItem = mainHandItem.copy();

            if (!food$waitingForSwitchOut && food$trigger(food$renderedMainHandItem, "switchout")) {
                food$switchOutItem = food$renderedMainHandItem.copy();
                food$pendingMainHandItem = mainHandItem.copy();
                food$waitingForSwitchOut = true;
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void food$triggerSwitchIn(CallbackInfo ci) {
        if (!food$hasTrackedSelectedItem) {
            return;
        }

        if (food$waitingForSwitchOut) {
            if (Empty.isClientAnimationPlaying(food$switchOutItem, "switchout")) {
                mainHandItem = food$switchOutItem.copy();
                mainHandHeight = 1.0F;
                oMainHandHeight = 1.0F;
                return;
            }

            mainHandItem = food$pendingMainHandItem.copy();
            mainHandHeight = 0.0F;
            oMainHandHeight = 0.0F;
            food$renderedMainHandItem = mainHandItem.copy();
            food$waitingForSwitchOut = false;
            food$switchOutItem = ItemStack.EMPTY;
            food$pendingMainHandItem = ItemStack.EMPTY;

            food$trigger(mainHandItem, "switchin");
            return;
        }

        if (!food$hasTrackedRenderedItem) {
            food$renderedMainHandItem = mainHandItem.copy();
            food$hasTrackedRenderedItem = true;
            return;
        }

        if (!ItemStack.isSameItemSameComponents(food$renderedMainHandItem, mainHandItem)) {
            food$trigger(mainHandItem, "switchin");
            food$renderedMainHandItem = mainHandItem.copy();
        }
    }

    @Unique
    private static boolean food$trigger(ItemStack stack, String controllerName) {
        if (!stack.isEmpty() &&FoodDefinitionManager.init_ItemList.contains(stack.getItem()) && AnimationResourceChecker.hasAnimation(stack, controllerName)) {
            Empty.triggerClientAnimation(stack, controllerName);
            return true;
        }
        return false;
    }

    @Unique
    private void food$resetTracking() {
        food$selectedMainHandItem = ItemStack.EMPTY;
        food$renderedMainHandItem = ItemStack.EMPTY;
        food$pendingMainHandItem = ItemStack.EMPTY;
        food$switchOutItem = ItemStack.EMPTY;
        food$hasTrackedSelectedItem = false;
        food$hasTrackedRenderedItem = false;
        food$waitingForSwitchOut = false;
    }
}

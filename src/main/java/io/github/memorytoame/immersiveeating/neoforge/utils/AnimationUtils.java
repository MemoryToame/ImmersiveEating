package io.github.memorytoame.immersiveeating.neoforge.utils;

import io.github.memorytoame.immersiveeating.neoforge.network.packet.StopAnimationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import io.github.memorytoame.immersiveeating.neoforge.additions.Empty;
import io.github.memorytoame.immersiveeating.neoforge.additions.definiton.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.mixin.Accessor.ItemInHandRendererAccessor;
import net.neoforged.neoforge.network.PacketDistributor;


public class AnimationUtils {
    public static ItemStack temp;
    public static int lockedHotbarSlot = -1;
    public static boolean wasUsingCustomItem;
    public static boolean customUseReequipHandled;
    public static ItemStack customUseStack = ItemStack.EMPTY;
    public static boolean wasEatAnimationPlaying;
    public static boolean isAnimationLocked() {
        return temp != null && lockedHotbarSlot >= 0;
    }

    public static void checkEatAnimationState() {
        boolean animationPlaying = Empty.isEatAnimationPlaying();
        if (animationPlaying) {
            wasEatAnimationPlaying = true;
        } else if (wasEatAnimationPlaying && isAnimationLocked()) {
            stopAnimationControl();
        }
    }

    public static void stopAnimationControl() {
        if (isAnimationLocked()) {
            PacketDistributor.sendToServer(new StopAnimationPacket());
        }
        Empty.stopEatAnimation();
        temp = null;
        lockedHotbarSlot = -1;
        wasEatAnimationPlaying = false;
    }

    public static void updateCustomUseState(Minecraft mc) {
        if (mc.player == null) {
            resetCustomUseState();
            return;
        }
        boolean usingCustomItem = mc.player.isUsingItem() && mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND &&
                FoodDefinitionManager.init_ItemList.contains(mc.player.getUseItem().getItem());

        if (usingCustomItem) {
            if (!wasUsingCustomItem) {
                customUseStack = mc.player.getUseItem().copy();
                customUseReequipHandled = false;
            }
            wasUsingCustomItem = true;
            return;
        }
        if (!wasUsingCustomItem) {
            return;
        }
        if (!customUseReequipHandled) {
            playCustomUseReequipAnimation(mc, customUseStack);
        }
        resetCustomUseState();
    }

    //切换物品的假动画 实际未切换
    public static void playCustomUseReequipAnimation(Minecraft mc, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ItemInHandRendererAccessor renderer = (ItemInHandRendererAccessor) mc.gameRenderer.itemInHandRenderer;
        ItemStack fakeStack = stack.copy();
        fakeStack.setCount(stack.getCount() + 1);

        renderer.setMainHandItem(fakeStack);
        customUseReequipHandled = true;
    }
    private static void resetCustomUseState() {
        wasUsingCustomItem = false;
        customUseReequipHandled = false;
        customUseStack = ItemStack.EMPTY;
    }
}

package io.github.memorytoame.immersiveeating.neoforge.events;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.additions.CustomRenderer;
import io.github.memorytoame.immersiveeating.neoforge.additions.Empty;
import io.github.memorytoame.immersiveeating.neoforge.additions.definiton.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.client.key.ModKeyMappings;
import io.github.memorytoame.immersiveeating.neoforge.mixin.Accessor.ItemInHandRendererAccessor;
import io.github.memorytoame.immersiveeating.neoforge.network.Network;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.AnimationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import static io.github.memorytoame.immersiveeating.neoforge.Food.lockedHotbarSlot;
import static io.github.memorytoame.immersiveeating.neoforge.Food.temp;


@EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
public class InputEvents {
    // InputEvents.java fields
    private static boolean wasUsingCustomItem;
    private static boolean customUseReequipHandled;
    private static ItemStack customUseStack = ItemStack.EMPTY;
    private static boolean wasEatAnimationPlaying;

    public static float debugX= -1.5F;
    public static float debugY= 0.05F;
    public static float debugZ= -0.3F;

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (isAnimationLocked()) {
            return;
        }
        if (ModKeyMappings.ANIMATION.consumeClick()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                try {
                    ItemStack stack = mc.player.getMainHandItem();
                    if (FoodDefinitionManager.init_ItemList.contains(stack.getItem())){
                        temp = stack.copy();
                        lockedHotbarSlot = mc.player.getInventory().selected;
                        PacketDistributor.sendToServer(new AnimationPacket());
                    }
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        }
        if (Food.DEBUG){
            if(event.getAction() != GLFW.GLFW_PRESS)
                return;
            switch (event.getKey()) {
                case GLFW.GLFW_KEY_I:
                    debugX+=0.05f;
                    break;
                case GLFW.GLFW_KEY_O:
                    debugY+=0.05f;
                    break;
                case GLFW.GLFW_KEY_P:
                    debugZ+=0.05f;
                    break;
                case GLFW.GLFW_KEY_J:
                    debugX-=0.05f;
                    break;
                case GLFW.GLFW_KEY_K:
                    debugY-=0.05f;
                    break;
                case GLFW.GLFW_KEY_L:
                    debugZ-=0.05f;
                    break;
            }
            System.out.println("translate = " + debugX+"," +debugY+","+debugZ);
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (isAnimationLocked()) {
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS && isAnimationLocked()) {
            //左键取消
            stopAnimationControl();
//            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            lockedHotbarSlot = -1;
            wasEatAnimationPlaying = false;
            return;
        }
        updateCustomUseState(mc);
        if (mc.screen != null) {
            //玩家进入GUI等界面 例如说esc
            if (temp != null || lockedHotbarSlot >= 0) {
                //不出现意外就正常暂停动画 以及解除物品栏锁
                stopAnimationControl();
            }
            return;
        }
        //检查是否在播放动画 如果没用播放而且锁还在 那就走正常暂停动画以及解锁物品栏锁 这一流程(防止一些奇奇怪怪的bug 动画没了锁还在)
        checkEatAnimationState();
        if (isAnimationLocked()) {
            //沿用锁
            mc.player.getInventory().selected = lockedHotbarSlot;
        } else {
            //将锁格=-1 dis掉
            lockedHotbarSlot = -1;
        }
    }

    public static boolean isAnimationLocked() {
        return temp != null && lockedHotbarSlot >= 0;
    }
    private static void checkEatAnimationState() {
        boolean animationPlaying = Empty.isEatAnimationPlaying();
        if (animationPlaying) {
            wasEatAnimationPlaying = true;
        } else if (wasEatAnimationPlaying && isAnimationLocked()) {
            stopAnimationControl();
        }
    }

    private static void stopAnimationControl() {
        Empty.stopEatAnimation();
        temp = null;
        lockedHotbarSlot = -1;
        wasEatAnimationPlaying = false;
    }

    private static void updateCustomUseState(Minecraft mc) {
        if (mc.player == null) {
            resetCustomUseState();
            return;
        }
        boolean usingCustomItem = mc.player.isUsingItem()
                && mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND
                && FoodDefinitionManager.init_ItemList.contains(mc.player.getUseItem().getItem());

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

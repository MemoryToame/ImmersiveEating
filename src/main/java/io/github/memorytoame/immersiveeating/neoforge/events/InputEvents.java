package io.github.memorytoame.immersiveeating.neoforge.events;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.additions.CustomRenderer;
import io.github.memorytoame.immersiveeating.neoforge.additions.Empty;
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
                    if (CustomRenderer.init_ItemList.contains(stack.getItem())){
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
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            lockedHotbarSlot = -1;
            return;
        }
        updateCustomUseState(mc);
        if (mc.screen != null) {
            if (temp != null || lockedHotbarSlot >= 0) {
                Empty.stopEatAnimation();
                temp = null;
                lockedHotbarSlot = -1;
            }
            return;
        }
        if (isAnimationLocked()) {
            mc.player.getInventory().selected = lockedHotbarSlot;
        } else {
            lockedHotbarSlot = -1;
        }
    }

    public static boolean isAnimationLocked() {
        return temp != null && lockedHotbarSlot >= 0;
    }

    private static void updateCustomUseState(Minecraft mc) {
        if (mc.player == null) {
            resetCustomUseState();
            return;
        }
        boolean usingCustomItem = mc.player.isUsingItem()
                && mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND
                && CustomRenderer.init_ItemList.contains(mc.player.getUseItem().getItem());

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

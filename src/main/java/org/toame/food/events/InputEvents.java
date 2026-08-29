package org.toame.food.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.toame.food.Food;
import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.client.key.ModKeyMappings;
import org.toame.food.network.Network;
import org.toame.food.network.packet.AnimationPacket;

import static org.toame.food.Food.*;
import static org.toame.food.utils.AnimationUtils.*;

@Mod.EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
public class InputEvents {

    public static float debugX= -1.5F;
    public static float debugY= 0.05F;
    public static float debugZ= -0.3F;

    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (ModKeyMappings.ANIMATION.consumeClick()) {
            if (isAnimationLocked()) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                try {
                    ItemStack stack = mc.player.getMainHandItem();
                    if (FoodDefinitionManager.init_ItemList.contains(stack.getItem())){

                        temp = stack.copy();
                        lockedHotbarSlot = mc.player.getInventory().selected;
                        Network.CHANNEL.sendToServer(new AnimationPacket());
                    }
                }catch (Exception exception){
                    exception.printStackTrace();
                }
            }
        }
        if (DEBUG){
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
        if (isAnimationLocked() && event.getAction() == GLFW.GLFW_PRESS && Minecraft.getInstance().options.keyUse.matchesMouse(event.getButton())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            lockedHotbarSlot = -1;
            wasEatAnimationPlaying = false;
            return;
        }
        updateCustomUseState(mc);
        if ((mc.screen != null && !(mc.screen instanceof ChatScreen))||!mc.options.getCameraType().isFirstPerson()) {
            //玩家进入GUI等界面//非第一人称 例如说esc
            if (isAnimationLocked()) {
                //不出现意外就正常暂停动画 以及解除物品栏锁
                stopAnimationControl();
            }
            return;
        }

        //检查是否在播放动画 如果没用播放而且锁还在 那就走正常暂停动画以及解锁物品栏锁 这一流程(防止一些奇奇怪怪的bug 动画没了锁还在)
        checkEatAnimationState();
        //使用期间
        if (isAnimationLocked()) {
            //沿用锁
            mc.player.getInventory().selected = lockedHotbarSlot;
        } else {
            //将锁格=-1 dis掉
            lockedHotbarSlot = -1;
        }
    }

}

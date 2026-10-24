package org.toame.food.events;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import org.toame.food.Food;
import org.toame.food.additions.Empty;
import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.network.Network;
import org.toame.food.network.packet.AnimationPacket;
import org.toame.food.utils.AnimationUtils;
import org.toame.food.client.key.ModKeyMappings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

import static org.toame.food.Food.DEBUG;
import static org.toame.food.config.Config.ENABLE_RIGHT_BUTTON;
import static org.toame.food.utils.AnimationUtils.*;

@Mod.EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
public class InputEvents {
    //    NORMAL(-1.05F,-0.35F,-0.8F,0.55F);
    public static float debugX= -1.05F;

    public static float debugY= -0.35F;

    public static float debugZ= -0.8F;


    @SubscribeEvent
    public static void onKey(InputEvent.Key event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        if (ModKeyMappings.ANIMATION.consumeClick()) {
            if (isAnimationLocked() || isAnimationRequestPending()) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                try {
                    ItemStack stack = mc.player.getMainHandItem();
                    if (FoodDefinitionManager.init_ItemList.contains(stack.getItem())){
                        AnimationUtils.beginAnimationSession(mc);
                        Network.CHANNEL.sendToServer(new AnimationPacket(AnimationUtils.getAnimationSessionId()));
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
                    debugX+=0.01f;
                    break;
                case GLFW.GLFW_KEY_O:
                    debugY+=0.01f;
                    break;
                case GLFW.GLFW_KEY_P:
                    debugZ+=0.01f;
                    break;
                case GLFW.GLFW_KEY_J:
                    debugX-=0.01f;
                    break;
                case GLFW.GLFW_KEY_K:
                    debugY-=0.01f;
                    break;
                case GLFW.GLFW_KEY_L:
                    debugZ-=0.01f;
                    break;
            }
            System.out.println("translate = " + debugX+"," +debugY+","+debugZ);
        }
    }

    @SubscribeEvent

    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (Minecraft.getInstance().player == null) {
            return;
        }
        if (isAnimationLocked()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMouseButton(InputEvent.MouseButton.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        if (event.getAction() == GLFW.GLFW_PRESS && mc.options.keyUse.matchesMouse(event.getButton()) && ENABLE_RIGHT_BUTTON.get() && !mc.player.isCrouching() && FoodDefinitionManager.init_ItemList.contains(mc.player.getMainHandItem().getItem()) && !isAnimationGui(mc.screen)) {
            if (Empty.isClientAnimationPlaying(mc.player.getMainHandItem(),"switchin")){
                return;
            }
            event.setCanceled(true);
            GameplayEvents.startRightClickAnimation();
            return;
        }

        if (isAnimationGui(mc.screen)) {
            return;
        }
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

    public static void onClientTick(ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            clearAnimationState();
            return;
        }
        updateCustomUseState(mc);
        if (!isAnimationGui(mc.screen)) {
            cancelIfHeldItemChanged(mc);
        }
        if ((mc.screen != null && !isAnimationGui(mc.screen)) || !mc.options.getCameraType().isFirstPerson()) {
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
            mc.player.swinging = false;
            mc.player.swingTime = 0;
            mc.player.attackAnim = 0.0F;
            mc.player.oAttackAnim = 0.0F;
            //沿用锁
            mc.player.getInventory().selected = lockedHotbarSlot;
        } else {
            //将锁格=-1 dis掉
            lockedHotbarSlot = -1;
        }
    }
    private static boolean isAnimationGui(Screen screen) {
        return screen instanceof ChatScreen || screen instanceof AbstractContainerScreen<?>;
    }
}

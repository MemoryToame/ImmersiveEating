package io.github.memorytoame.immersiveeating.neoforge.events;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.additions.CustomRenderer;
import io.github.memorytoame.immersiveeating.neoforge.additions.definiton.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.client.key.ModKeyMappings;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.AnimationPacket;
import io.github.memorytoame.immersiveeating.neoforge.utils.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import static io.github.memorytoame.immersiveeating.neoforge.utils.AnimationUtils.isAnimationLocked;

@EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
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
                        AnimationUtils.temp = stack.copy();
                        AnimationUtils.lockedHotbarSlot = mc.player.getInventory().selected;
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
            AnimationUtils.stopAnimationControl();
//            event.setCanceled(true);
        }
        if (isAnimationLocked() && event.getAction() == GLFW.GLFW_PRESS && Minecraft.getInstance().options.keyUse.matchesMouse(event.getButton())) {
            event.setCanceled(true);
        }

    }
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            AnimationUtils.lockedHotbarSlot = -1;
            AnimationUtils.wasEatAnimationPlaying = false;
            return;
        }
        AnimationUtils.updateCustomUseState(mc);
        if ((mc.screen != null && !(mc.screen instanceof ChatScreen))||!mc.options.getCameraType().isFirstPerson()) {
            //玩家进入GUI等界面//非第一人称 例如说esc
            if (isAnimationLocked()) {
                //不出现意外就正常暂停动画 以及解除物品栏锁
                AnimationUtils.stopAnimationControl();
            }
            return;
        }
        //检查是否在播放动画 如果没用播放而且锁还在 那就走正常暂停动画以及解锁物品栏锁 这一流程(防止一些奇奇怪怪的bug 动画没了锁还在)
        AnimationUtils.checkEatAnimationState();
        if (isAnimationLocked()) {
            //沿用锁
            mc.player.getInventory().selected = AnimationUtils.lockedHotbarSlot;
        } else {
            //将锁格=-1 dis掉
            AnimationUtils.lockedHotbarSlot = -1;
        }
    }
}

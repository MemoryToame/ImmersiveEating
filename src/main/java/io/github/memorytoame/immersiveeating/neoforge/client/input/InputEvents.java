package io.github.memorytoame.immersiveeating.neoforge.client.input;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.client.events.GameplayEvents;
import io.github.memorytoame.immersiveeating.neoforge.common.content.item.Empty;
import io.github.memorytoame.immersiveeating.neoforge.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.AnimationPacket;
import io.github.memorytoame.immersiveeating.neoforge.utils.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

import static io.github.memorytoame.immersiveeating.neoforge.Food.DEBUG;
import static io.github.memorytoame.immersiveeating.neoforge.common.config.Config.ENABLE_RIGHT_BUTTON;
import static io.github.memorytoame.immersiveeating.neoforge.utils.AnimationUtils.*;

@EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
public class InputEvents {
    public static float debugX= -1.5F;
    public static float debugY= 0.05F;
    public static float debugZ= -0.3F;
    public static float debugRo= 0.00F;

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
                    if (Empty.isClientAnimationPlaying(stack, "switchin")) {
                        return;
                    }
                    if (FoodDefinitionManager.init_ItemList.contains(stack.getItem())){
                        AnimationUtils.beginAnimationSession(mc);
                        PacketDistributor.sendToServer(new AnimationPacket(AnimationUtils.getAnimationSessionId(mc)));
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
                case GLFW.GLFW_KEY_M:
                    debugRo+=0.01f;
                    break;
                case GLFW.GLFW_KEY_N:
                    debugRo-=0.01f;
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
    public static void onClientTick(ClientTickEvent.Post event) {
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


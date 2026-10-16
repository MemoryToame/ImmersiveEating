package io.github.memorytoame.immersiveeating.neoforge.client.events;


import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.common.content.item.Empty;
import io.github.memorytoame.immersiveeating.neoforge.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.AnimationPacket;
import io.github.memorytoame.immersiveeating.neoforge.utils.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import static io.github.memorytoame.immersiveeating.neoforge.common.config.Config.ENABLE_RIGHT_BUTTON;
/*
主要针对阻止方块放置 如果开启了右键触发动画）
 */
@EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
public class GameplayEvents {
//    private static boolean rightClickAnimationStarted;
//    /*
//    针对可交互方块 在互动后1tick 准星为空气触发动画
//     */
//    private static boolean rightClickBlockInteraction;
//
//    /*
//    玩家右键也可以触发动画
//     */
//    @SubscribeEvent
//    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
//        Minecraft mc = Minecraft.getInstance();
//        if (ENABLE_RIGHT_BUTTON.get()&&FoodDefinitionManager.init_ItemList.contains(mc.player.getMainHandItem().getItem())){
//            event.setCanceled(true);
//
//        }
//    }
//
//    @SubscribeEvent
//    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
//        if (!ENABLE_RIGHT_BUTTON.get() || event.getEntity() != Minecraft.getInstance().player) {
//            return;
//        }
//
//        if (FoodDefinitionManager.init_ItemList.contains(event.getItemStack().getItem())) {
//            rightClickBlockInteraction = true;
//        }
//    }
//    @SubscribeEvent
//    public static void onClientTick(ClientTickEvent.Post event) {
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.player == null || mc.screen != null || !ENABLE_RIGHT_BUTTON.get()) {
//            rightClickAnimationStarted = false;
//            rightClickBlockInteraction = false;
//            return;
//        }
//
//        if (!mc.options.keyUse.isDown()) {
//            rightClickAnimationStarted = false;
//            rightClickBlockInteraction = false;
//            return;
//        }
//
//        if (rightClickAnimationStarted || AnimationUtils.isAnimationLocked() || AnimationUtils.isAnimationRequestPending()) {
//            return;
//        }
//
//        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
//            rightClickBlockInteraction = true;
//            return;
//        }
//        if (rightClickBlockInteraction) {
//            return;
//        }
//
//        ItemStack stack = mc.player.getMainHandItem();
//        if (Empty.isClientAnimationPlaying(stack, "switchin") || !FoodDefinitionManager.init_ItemList.contains(stack.getItem())) {
//            return;
//        }
//
//        startRightClickAnimation(mc, stack);
//    }

    @SubscribeEvent
    public static void onRightClick(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (!ENABLE_RIGHT_BUTTON.get() || mc.player == null || mc.player.isCrouching() || !event.isUseItem()) {
            return;
        }

        ItemStack stack = mc.player.getMainHandItem();
        if (!FoodDefinitionManager.init_ItemList.contains(stack.getItem())) {
            return;
        }
        if (Empty.isClientAnimationPlaying(stack,"switchin")){
            return;
        }
        event.setCanceled(true);
        startRightClickAnimation(stack);
    }
    public static void startRightClickAnimation() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            startRightClickAnimation(mc.player.getMainHandItem());
        }
    }
    private static void startRightClickAnimation(ItemStack stack) {
    Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || AnimationUtils.isAnimationLocked() || AnimationUtils.isAnimationRequestPending() || Empty.isClientAnimationPlaying(stack, "switchin")
                || !FoodDefinitionManager.init_ItemList.contains(stack.getItem())) {
            return;
        }
        AnimationUtils.beginAnimationSession(mc);
        PacketDistributor.sendToServer(new AnimationPacket(AnimationUtils.getAnimationSessionId(mc)));
    }
}

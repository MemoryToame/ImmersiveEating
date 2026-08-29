package io.github.memorytoame.immersiveeating.neoforge.events;


import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.additions.definiton.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.AnimationPacket;
import io.github.memorytoame.immersiveeating.neoforge.utils.AnimationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import static io.github.memorytoame.immersiveeating.neoforge.config.Config.ENABLE_RIGHT_BUTTON;

@EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
public class GameplayEvents {
    /*
    玩家右键也可以触发动画
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (AnimationUtils.isAnimationLocked()) {
            event.setCanceled(true);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null&&ENABLE_RIGHT_BUTTON.get()) {
            try {
                ItemStack stack = mc.player.getMainHandItem();
                if (FoodDefinitionManager.init_ItemList.contains(stack.getItem())){
                    event.setCanceled(true);
                    AnimationUtils.temp = stack.copy();
                    AnimationUtils.lockedHotbarSlot = mc.player.getInventory().selected;
                   PacketDistributor.sendToServer(new AnimationPacket());
                }
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }
}

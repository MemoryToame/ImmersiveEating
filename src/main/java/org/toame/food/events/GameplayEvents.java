package org.toame.food.events;


import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.toame.food.Food;
import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.network.Network;
import org.toame.food.network.packet.AnimationPacket;
import org.toame.food.utils.AnimationUtils;

import static org.toame.food.config.Config.ENABLE_RIGHT_BUTTON;

@Mod.EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
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
                    Network.CHANNEL.sendToServer(new AnimationPacket());
                }
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }
    }
}

package org.toame.food.events;

import net.minecraft.world.InteractionHand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.toame.food.Food;
import org.toame.food.network.ServerAnimationManager;

import static org.toame.food.config.Config.ENABLE_RIGHT_BUTTON;

@Mod.EventBusSubscriber(modid = Food.MODID)
public final class ServerGameplayEvents {
    private ServerGameplayEvents() {
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || ServerAnimationManager.get(player) == null) {
            return;
        }
        if (event instanceof PlayerInteractEvent.RightClickBlock && ContainerTarget.allowsInteraction(event.getLevel(), event.getPos())) {
            return;
        }
        if (event instanceof PlayerInteractEvent.EntityInteract interact && ContainerTarget.allowsInteraction(interact.getTarget())) {
            return;
        }
        if (event instanceof PlayerInteractEvent.EntityInteractSpecific specific && ContainerTarget.allowsInteraction(specific.getTarget())) {
            return;
        }
        if (!ENABLE_RIGHT_BUTTON.get() || event.getEntity().isCrouching() || event.getHand() != InteractionHand.MAIN_HAND
                || !isRightClick(event)) {
            return;
        }

        if (ServerAnimationManager.matchesHeldStack(player)) {
            event.setCanceled(true);
        }
    }

    private static boolean isRightClick(PlayerInteractEvent event) {
        return event instanceof PlayerInteractEvent.RightClickBlock
                || event instanceof PlayerInteractEvent.RightClickItem
                || event instanceof PlayerInteractEvent.RightClickEmpty
                || event instanceof PlayerInteractEvent.EntityInteract
                || event instanceof PlayerInteractEvent.EntityInteractSpecific;
    }
}

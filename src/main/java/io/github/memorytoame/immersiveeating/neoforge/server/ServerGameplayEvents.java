package io.github.memorytoame.immersiveeating.neoforge.server;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.common.content.definition.FoodDefinitionManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import static io.github.memorytoame.immersiveeating.neoforge.common.config.Config.ENABLE_RIGHT_BUTTON;

@EventBusSubscriber(modid = Food.MODID)
public final class ServerGameplayEvents {
    private ServerGameplayEvents() {
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!ENABLE_RIGHT_BUTTON.get() || !(event.getEntity() instanceof ServerPlayer player) || player.isCrouching()) {
            return;
        }

        var session = ServerAnimationManager.get(player);
        if (session != null && session.hand() == event.getHand() && ServerAnimationManager.matchesHeldStack(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new FoodDefinitionManager());
    }
}

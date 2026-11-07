package io.github.memorytoame.immersiveeating.neoforge.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ServerAnimationManager {
    private static final Map<UUID, AnimationState> ACTIVE_ANIMATIONS = new HashMap<>();

    private ServerAnimationManager() {
    }

    public static AnimationState get(ServerPlayer player) {
        return player == null ? null : ACTIVE_ANIMATIONS.get(player.getUUID());
    }

    public static void begin(ServerPlayer player, UUID sessionId, InteractionHand hand, ItemStack stack) {
        // A fresh request supersedes an abandoned session. Finish and stop must match its UUID.
        ACTIVE_ANIMATIONS.put(player.getUUID(), new AnimationState(player.getUUID(), sessionId, hand, stack.copy()));
    }

    public static boolean matchesSession(ServerPlayer player, UUID sessionId, InteractionHand hand) {
        AnimationState state = get(player);
        return player != null && state != null && state.ownerId().equals(player.getUUID()) && state.sessionId().equals(sessionId) && state.hand() == hand;
    }

    public static boolean matchesHeldStack(ServerPlayer player) {
        AnimationState state = get(player);
        if (state == null) {
            return false;
        }

        if (!state.ownerId().equals(player.getUUID())) {
            return false;
        }
        ItemStack heldStack = player.getItemInHand(state.hand());
        ItemStack expectedStack = state.stack();
        return !heldStack.isEmpty() && ItemStack.isSameItemSameComponents(heldStack, expectedStack)
                && heldStack.getCount() == expectedStack.getCount();
    }


    public static void clear(ServerPlayer player) {
        if (player != null) {
            ACTIVE_ANIMATIONS.remove(player.getUUID());
        }
    }

    public static void clear(UUID uuid) {
        if (uuid != null) {
            ACTIVE_ANIMATIONS.remove(uuid);
        }
    }

    public static void clearAll() {
        ACTIVE_ANIMATIONS.clear();
    }

    public record AnimationState(UUID ownerId, UUID sessionId, InteractionHand hand, ItemStack stack) {
    }
}


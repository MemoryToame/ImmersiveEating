package io.github.memorytoame.immersiveeating.neoforge.client.animation;

import io.github.memorytoame.immersiveeating.neoforge.init.ModItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.GeckoLibConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ClientAnimationManager {
    private static final Map<UUID, AnimationState> PLAYER_ANIMATIONS = new HashMap<>();

    private ClientAnimationManager() {
    }

    public static long getOrCreateControllerId(LocalPlayer player) {
        if (player == null) {
            return Long.MAX_VALUE;
        }
        return PLAYER_ANIMATIONS.computeIfAbsent(player.getUUID(), id -> new AnimationState(controllerIdFor(id), null)).controllerId();
    }

    public static UUID beginSession(LocalPlayer player) {
        UUID playerId = player.getUUID();
        AnimationState state = PLAYER_ANIMATIONS.computeIfAbsent(playerId, id -> new AnimationState(controllerIdFor(id), null));
        UUID sessionId = UUID.randomUUID();
        PLAYER_ANIMATIONS.put(playerId, new AnimationState(state.controllerId(), sessionId));
        return sessionId;
    }

    public static UUID getSessionId(LocalPlayer player) {
        if (player == null) {
            return null;
        }
        AnimationState state = PLAYER_ANIMATIONS.get(player.getUUID());
        return state == null ? null : state.sessionId();
    }

    public static void clearSession(LocalPlayer player) {
        if (player == null) {
            return;
        }
        AnimationState state = PLAYER_ANIMATIONS.get(player.getUUID());
        if (state != null) {
            PLAYER_ANIMATIONS.put(player.getUUID(), new AnimationState(state.controllerId(), null));
        }
    }

    public static void clearAll() {
        PLAYER_ANIMATIONS.clear();
    }

    private static long controllerIdFor(UUID playerId) {
        long id = playerId.getMostSignificantBits() ^ Long.rotateLeft(playerId.getLeastSignificantBits(), 1);
        id &= Long.MAX_VALUE;
        return id == 0 ? 1 : id;
    }

    private record AnimationState(long controllerId, UUID sessionId) {
    }
}

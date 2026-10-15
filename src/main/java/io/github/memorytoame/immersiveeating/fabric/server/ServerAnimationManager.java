package io.github.memorytoame.immersiveeating.fabric.server;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;

public final class ServerAnimationManager {
   private static final Map<UUID, ServerAnimationManager.AnimationState> ACTIVE_ANIMATIONS = new HashMap<>(16);

   private ServerAnimationManager() {
   }

   public static ServerAnimationManager.AnimationState get(ServerPlayer player) {
      return player == null ? null : ACTIVE_ANIMATIONS.get(player.getUUID());
   }

   public static boolean begin(ServerPlayer player, UUID sessionId, InteractionHand hand, ItemStack stack, long controllerId) {
      if (player == null
         || sessionId == null
         || hand == null
         || stack == null
         || stack.isEmpty()
         || controllerId == Long.MAX_VALUE
         || GeoItem.getId(stack) != controllerId) {
         return false;
      }

      if (ACTIVE_ANIMATIONS.containsKey(player.getUUID())) {
         return false;
      }

      ACTIVE_ANIMATIONS.put(player.getUUID(), new ServerAnimationManager.AnimationState(player.getUUID(), sessionId, hand, stack.copy(), controllerId));
      return true;
   }

   public static boolean matchesSession(ServerPlayer player, UUID sessionId, InteractionHand hand) {
      ServerAnimationManager.AnimationState state = get(player);
      return player != null && state != null && state.ownerId().equals(player.getUUID()) && state.sessionId().equals(sessionId) && state.hand() == hand;
   }

   public static boolean matchesHeldStack(ServerPlayer player) {
      ServerAnimationManager.AnimationState state = get(player);
      if (state == null) {
         return false;
      }

      if (!state.ownerId().equals(player.getUUID())) {
         return false;
      }

      ItemStack heldStack = player.getItemInHand(state.hand());
      ItemStack expectedStack = state.stack();
      return !heldStack.isEmpty()
         && ItemStack.isSameItemSameComponents(heldStack, expectedStack)
         && heldStack.getCount() == expectedStack.getCount()
         && GeoItem.getId(heldStack) == state.controllerId();
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

   public record AnimationState(UUID ownerId, UUID sessionId, InteractionHand hand, ItemStack stack, long controllerId) {
   }
}


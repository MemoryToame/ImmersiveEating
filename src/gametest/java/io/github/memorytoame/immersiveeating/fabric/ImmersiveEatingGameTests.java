package io.github.memorytoame.immersiveeating.fabric;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;

/** Server-side smoke tests for the Fabric port. */
public final class ImmersiveEatingGameTests implements FabricGameTest {
   private static final ResourceLocation EMPTY_ITEM = ResourceLocation.fromNamespaceAndPath(
      KaleidoscopeImmersiveEating.MOD_ID,
      "empty"
   );

   @GameTest(template = FabricGameTest.EMPTY_STRUCTURE, timeoutTicks = 20)
   public void emptyItemIsRegistered(GameTestHelper helper) {
      if (!BuiltInRegistries.ITEM.containsKey(EMPTY_ITEM)) {
         helper.fail("Expected the server-side empty animation item to be registered");
         return;
      }

      helper.succeed();
   }
}

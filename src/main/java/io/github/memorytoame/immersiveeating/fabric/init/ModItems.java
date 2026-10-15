package io.github.memorytoame.immersiveeating.fabric.init;

import io.github.memorytoame.immersiveeating.fabric.KaleidoscopeImmersiveEating;
import io.github.memorytoame.immersiveeating.fabric.common.content.item.Empty;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public final class ModItems {
   public static final Empty EMPTY = Registry.register(
      BuiltInRegistries.ITEM,
      ResourceLocation.fromNamespaceAndPath(KaleidoscopeImmersiveEating.MOD_ID, "empty"),
      new Empty(new Item.Properties())
   );

   public static void init() {}

   private ModItems() {
   }
}


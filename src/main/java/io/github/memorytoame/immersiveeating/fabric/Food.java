package io.github.memorytoame.immersiveeating.fabric;

import io.github.memorytoame.immersiveeating.fabric.common.config.Config;
import io.github.memorytoame.immersiveeating.fabric.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.fabric.init.ModItems;
import io.github.memorytoame.immersiveeating.fabric.network.Network;
import io.github.memorytoame.immersiveeating.fabric.server.ServerGameplayEvents;
import io.github.memorytoame.immersiveeating.fabric.server.CommandEvents;
import io.github.memorytoame.immersiveeating.fabric.unique.UniqueItem;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;

public final class Food {
   public static boolean DEBUG = false;
   private static boolean initialized;

   private Food() {
   }

   public static void initialize() {
      if (initialized) {
         return;
      }
      initialized = true;
      Network.register();
      Config.register();
      UniqueItem.initUniqueItemMap();
      ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new FoodDefinitionManager());
      ServerGameplayEvents.register();
      CommandEvents.register();
      ModItems.init();
   }
}


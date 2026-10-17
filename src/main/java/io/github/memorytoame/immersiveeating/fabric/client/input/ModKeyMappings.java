package io.github.memorytoame.immersiveeating.fabric.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;

@Environment(EnvType.CLIENT)
public final class ModKeyMappings {
   public static final KeyMapping ANIMATION = new KeyMapping(
      "key.food.animation", InputConstants.Type.KEYSYM, InputConstants.KEY_U, "key.categories.food"
   );

   private ModKeyMappings() {
   }
}


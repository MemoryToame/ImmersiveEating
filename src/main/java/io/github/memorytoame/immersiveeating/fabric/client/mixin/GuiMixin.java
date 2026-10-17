package io.github.memorytoame.immersiveeating.fabric.client.mixin;

import io.github.memorytoame.immersiveeating.fabric.common.config.Config;
import io.github.memorytoame.immersiveeating.fabric.utils.AnimationUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
   @Inject(method = "renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
   private void food$hideHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
      if (!Config.HIDE_GUI_WHEN_EATING.get() && AnimationUtils.isAnimationLocked()) {
         ci.cancel();
      }
   }
}


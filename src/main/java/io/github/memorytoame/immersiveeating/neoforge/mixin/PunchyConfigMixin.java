package io.github.memorytoame.immersiveeating.neoforge.mixin;

import io.github.memorytoame.immersiveeating.neoforge.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.compat.Punchy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "punchy.config.PunchyConfig")
public class PunchyConfigMixin {

    @Inject(method = "isModEnabled()Z", at = @At("HEAD"), cancellable = true, require = 0)
    private static void food$disablePunchyForCustomItem(CallbackInfoReturnable<Boolean> cir) {
        Punchy.disablePunchyForCustomItem(cir);
    }
}

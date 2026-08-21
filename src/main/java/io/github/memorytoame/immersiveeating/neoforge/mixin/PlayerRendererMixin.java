package io.github.memorytoame.immersiveeating.neoforge.mixin;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    /*
    强制设置手臂为4像素即Steve皮肤
    发布版默认slim
     */
    @ModifyVariable(method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Z)V", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private static boolean food$forceWideArms(boolean slim) {
        return slim;
    }
}

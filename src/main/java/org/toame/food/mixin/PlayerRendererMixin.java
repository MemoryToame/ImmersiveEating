package org.toame.food.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.toame.food.Food;
import software.bernie.geckolib.cache.object.GeoBone;

import static org.toame.food.Food.*;

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

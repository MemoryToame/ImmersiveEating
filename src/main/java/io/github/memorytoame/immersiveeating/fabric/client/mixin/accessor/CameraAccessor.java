package io.github.memorytoame.immersiveeating.fabric.client.mixin.accessor;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** 用于同步摄影机渲染模型quad的自定义角度 */
@Mixin(Camera.class)
public interface CameraAccessor {
   @Invoker("setRotation")
   void food$setRotation(float yaw, float pitch);
}

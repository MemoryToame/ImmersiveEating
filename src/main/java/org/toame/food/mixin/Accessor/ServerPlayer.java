package org.toame.food.mixin.Accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.server.level.ServerPlayer.class)
public interface ServerPlayer {
    @Invoker("completeUsingItem")
    void food$invokeCompleteUsingItem();
}

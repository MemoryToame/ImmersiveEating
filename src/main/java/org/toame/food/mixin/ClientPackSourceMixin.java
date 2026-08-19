package org.toame.food.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.network.chat.Component;

import org.toame.food.additions.sound.FoodDynamicPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mixin(ClientPackSource.class)
public class ClientPackSourceMixin {
    @Inject(method = "populatePackList", at = @At("TAIL"))
    private void food$addDynamicPack(BiConsumer<String, Function<String, Pack>> consumer, CallbackInfo ci) {
        consumer.accept("food_dynamic", id -> Pack.readMetaAndCreate(id,
                        Component.literal("ImmersiveEating Resources"),
                        true,
                        packId -> new FoodDynamicPack(),
                        PackType.CLIENT_RESOURCES,
                        Pack.Position.TOP,
                        PackSource.BUILT_IN
                )
        );

    }
}
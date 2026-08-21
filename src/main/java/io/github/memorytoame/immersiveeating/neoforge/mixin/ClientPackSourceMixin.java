package io.github.memorytoame.immersiveeating.neoforge.mixin;

import io.github.memorytoame.immersiveeating.neoforge.additions.sound.FoodDynamicPack;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.Optional;

@Mixin(ClientPackSource.class)
public class ClientPackSourceMixin {
    @Inject(method = "populatePackList", at = @At("TAIL"))
    private void food$addDynamicPack(BiConsumer<String, Function<String, Pack>> consumer, CallbackInfo ci) {
        PackLocationInfo locationInfo = new PackLocationInfo(
                "food_dynamic",
                Component.literal("ImmersiveEating Resources"),
                PackSource.BUILT_IN,
                Optional.empty()
        );
        PackSelectionConfig selectionConfig = new PackSelectionConfig(
                true,
                Pack.Position.TOP,
                true
        );

        consumer.accept("food_dynamic", ignored -> Pack.readMetaAndCreate(
                locationInfo,
                new Pack.ResourcesSupplier() {
                    @Override
                    public PackResources openPrimary(PackLocationInfo ignoredLocation) {
                        return new FoodDynamicPack();
                    }

                    @Override
                    public PackResources openFull(PackLocationInfo ignoredLocation, Pack.Metadata metadata) {
                        return new FoodDynamicPack();
                    }
                },
                PackType.CLIENT_RESOURCES,
                selectionConfig
        ));

    }
}

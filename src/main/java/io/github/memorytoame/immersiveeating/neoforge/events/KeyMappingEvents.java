package io.github.memorytoame.immersiveeating.neoforge.events;


import io.github.memorytoame.immersiveeating.neoforge.client.key.ModKeyMappings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import static io.github.memorytoame.immersiveeating.neoforge.Food.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class KeyMappingEvents {

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.ANIMATION);
    }
}

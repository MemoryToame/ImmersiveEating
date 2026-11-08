package io.github.memorytoame.immersiveeating.neoforge.init;

import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.common.content.item.Empty;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Food.MODID);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static final DeferredItem<Empty> EMPTY = ITEMS.registerItem("empty", Empty::new, new Item.Properties());

}

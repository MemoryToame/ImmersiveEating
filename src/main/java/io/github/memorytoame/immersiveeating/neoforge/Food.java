package io.github.memorytoame.immersiveeating.neoforge;

import io.github.memorytoame.immersiveeating.neoforge.init.ModItems;
import io.github.memorytoame.immersiveeating.neoforge.network.Network;
import io.github.memorytoame.immersiveeating.neoforge.config.Config;
import io.github.memorytoame.immersiveeating.neoforge.unique.UniqueItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;


@Mod(Food.MODID)
public class Food {
    public static final String MODID = "food";
    public static final String AUTHOR= "XianYue";
    public static boolean DEBUG = false;

    public Food(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(Network::register);
        ModItems.register(modEventBus);
        UniqueItem.initUniqueItemMap();
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "immersive_eating.toml");
    }
}

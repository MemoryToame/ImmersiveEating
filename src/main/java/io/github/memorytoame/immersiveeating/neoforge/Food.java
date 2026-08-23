package io.github.memorytoame.immersiveeating.neoforge;

import io.github.memorytoame.immersiveeating.neoforge.init.ModItems;
import io.github.memorytoame.immersiveeating.neoforge.network.Network;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;


@Mod(Food.MODID)
public class Food {
    public static final String MODID = "food";
    public static final String AUTHOR= "XianYue";
    public static ItemStack temp;
    public static boolean DEBUG = false;
    public static int lockedHotbarSlot = -1;

    public Food(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(Network::register);
        ModItems.register(modEventBus);
    }
}

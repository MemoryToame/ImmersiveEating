package org.toame.food;

import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.toame.food.config.Config;
import org.toame.food.init.*;
import org.toame.food.network.Network;
import org.toame.food.unique.UniqueItem;
import software.bernie.geckolib.GeckoLib;


@Mod(Food.MODID)
public class Food {

    public static final String MODID = "food";
    public static final boolean DEBUG = false;
    public static final String AUTHOR= "XianYue";
    public Food() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        GeckoLib.initialize();
        Network.register();
        ModItems.register(modEventBus);
        UniqueItem.initUniqueItemMap();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC,"immersive_eating.toml");
        //end region
        modEventBus.register(this);
    }
}

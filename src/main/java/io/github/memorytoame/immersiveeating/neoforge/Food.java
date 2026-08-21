package io.github.memorytoame.immersiveeating.neoforge;

import com.mojang.logging.LogUtils;
import io.github.memorytoame.immersiveeating.neoforge.init.ModItems;
import io.github.memorytoame.immersiveeating.neoforge.network.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;
import software.bernie.geckolib.GeckoLibClient;


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
        GeckoLibClient.init();
    }
}

package org.toame.food.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.toame.food.Food;
import org.toame.food.network.packet.AnimationPacket;
import org.toame.food.network.packet.SoundPacket;

public class Network {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(Food.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);
    public static void register() {
        CHANNEL.registerMessage(0, AnimationPacket.class, AnimationPacket::encode, AnimationPacket::decode, AnimationPacket::handle);
        CHANNEL.registerMessage(1, SoundPacket.class, SoundPacket::encode, SoundPacket::decode, SoundPacket::handle);
    }
}

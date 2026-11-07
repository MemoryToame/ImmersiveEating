package org.toame.food.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.toame.food.Food;
import org.toame.food.network.packet.*;

public final class Network {
    private static final String VERSION = "4";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Food.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private Network() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, AnimationPacket.class, AnimationPacket::encode, AnimationPacket::decode, AnimationPacket::handle);
        CHANNEL.registerMessage(id++, FinishUsePacket.class, FinishUsePacket::encode, FinishUsePacket::decode, FinishUsePacket::handle);
        CHANNEL.registerMessage(id++, CustomUsingPacket.class, CustomUsingPacket::encode, CustomUsingPacket::decode, CustomUsingPacket::handle);
        CHANNEL.registerMessage(id++, StopAnimationPacket.class, StopAnimationPacket::encode, StopAnimationPacket::decode, StopAnimationPacket::handle);
        CHANNEL.registerMessage(id++, StopSoundPacket.class, StopSoundPacket::encode, StopSoundPacket::decode, StopSoundPacket::handle);
        CHANNEL.messageBuilder(AnimationStatusPacket.class, id, net.minecraftforge.network.NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AnimationStatusPacket::encode).decoder(AnimationStatusPacket::decode)
                .consumerNetworkThread(AnimationStatusPacket::handle).add();
    }
}

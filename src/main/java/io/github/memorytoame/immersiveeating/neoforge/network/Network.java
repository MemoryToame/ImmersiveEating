package io.github.memorytoame.immersiveeating.neoforge.network;

import io.github.memorytoame.immersiveeating.neoforge.network.packet.AnimationPacket;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.FinishUsePacket;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.SoundPacket;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class Network {
    private static final String VERSION = "1";

    private Network() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);

        registrar.playToServer(AnimationPacket.TYPE, AnimationPacket.STREAM_CODEC, AnimationPacket::handle);
        registrar.playToServer(SoundPacket.TYPE, SoundPacket.STREAM_CODEC, SoundPacket::handle);
        registrar.playToServer(FinishUsePacket.TYPE, FinishUsePacket.STREAM_CODEC, FinishUsePacket::handle);
    }
}

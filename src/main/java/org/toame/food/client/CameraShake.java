package org.toame.food.client;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.toame.food.Food;

import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber(modid = Food.MODID, value = Dist.CLIENT)
public final class CameraShake {

//    public static float debug_y=1.2f;
//    public static float debug_p=0.30f;
//    public static float debug_r=0.03f;
    private static float yaw;
    private static float pitch;
    private static float roll;

    private static float yawVel;
    private static float pitchVel;
    private static float rollVel;

    private static float prevYaw;
    private static float prevPitch;
    private static float prevRoll;

    private CameraShake() {}

    public static void trigger() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        float pitchStrength = random.nextFloat(3.2F, 4.0F);
        float yawStrength = random.nextFloat(0.2F, 0.6F);
//        float rollStrength = random.nextFloat(0.5F, 1.1F);
        float rollStrength = random.nextFloat(1.5F, 2.1F);


        pitchVel += random.nextBoolean() ? pitchStrength : -pitchStrength;
        yawVel += random.nextBoolean() ? yawStrength : -yawStrength;
        rollVel += random.nextBoolean() ? rollStrength : -rollStrength;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        tick();
    }
    private static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            reset();
            return;
        }
        prevYaw = yaw;
        prevPitch = pitch;
        prevRoll = roll;

        yawVel += -yaw * 0.25F;
        yawVel *= 0.68F;
        yaw += yawVel;

        pitchVel += -pitch * 0.25F;
        pitchVel *= 0.68F;
        pitch += pitchVel;

        rollVel += -roll * 0.25F;
        rollVel *= 0.68F;
        roll += rollVel;

        yaw = Mth.clamp(yaw, -2.0F, 2.0F);
        pitch = Mth.clamp(pitch, -4.0F, 4.0F);
        roll = Mth.clamp(roll, -2.0F, 2.0F);
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        float partialTick = (float) event.getPartialTick();

        //插帧后平滑
        float interpYaw = Mth.lerp(partialTick, prevYaw, yaw);
        float interpPitch = Mth.lerp(partialTick, prevPitch, pitch);
        float interpRoll = Mth.lerp(partialTick, prevRoll, roll);

        event.setYaw(event.getYaw() + interpYaw);
        event.setPitch(event.getPitch() + interpPitch);
        event.setRoll(event.getRoll() + interpRoll);
    }

    public static void reset() {
        yaw = 0.0F;
        pitch = 0.0F;
        roll = 0.0F;

        yawVel = 0.0F;
        pitchVel = 0.0F;
        rollVel = 0.0F;

        prevYaw = 0.0F;
        prevPitch = 0.0F;
        prevRoll = 0.0F;
    }
}

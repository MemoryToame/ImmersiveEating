package org.toame.food.additions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.toame.food.Food;
import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.network.Network;
import org.toame.food.network.packet.FinishUsePacket;
import org.toame.food.network.packet.SoundPacket;
import org.toame.food.init.ModItems;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class Empty extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack stack;
    private static Consumer<ItemStack> clientUseReequipHandler = ignored -> {};
    private static Runnable clientCameraShakeHandler = () -> {};

    public Empty(Properties pProperties) {
        super(pProperties);
        GeoItem.registerSyncedAnimatable(this);
    }

    public static void setClientAnimationHooks(Consumer<ItemStack> useReequipHandler, Runnable cameraShakeHandler) {
        clientUseReequipHandler = useReequipHandler;
        clientCameraShakeHandler = cameraShakeHandler;
    }
    public ItemStack getRenderStack() {
        if (stack == null) {
            stack = new ItemStack(this);
        }
        return stack;
    }

    public static void stopEatAnimation() {
        Empty empty = ModItems.EMPTY.get();
        long instanceId = GeoItem.getId(empty.getRenderStack());
        AnimationController<?> controller = empty.getAnimatableInstanceCache()
                .getManagerForId(instanceId)
                .getAnimationControllers()
                .get("eat");
        if (controller != null) {
            controller.stop();
        }
    }

    public static boolean isEatAnimationPlaying() {
        Empty empty = ModItems.EMPTY.get();
        long instanceId = GeoItem.getId(empty.getRenderStack());
        AnimationController<?> controller = empty.getAnimatableInstanceCache()
                .getManagerForId(instanceId)
                .getAnimationControllers()
                .get("eat");
        return controller != null && controller.isPlayingTriggeredAnimation();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        AnimationController<Empty> controller = new AnimationController<>(this, "eat", state -> PlayState.STOP);
        controller.triggerableAnim("eat", RawAnimation.begin().thenPlay("eat"));
        controller.setSoundKeyframeHandler(keyFrames ->{
            if (Food.temp == null) {
                return;
            }
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(Food.temp.getItem());
            if (itemId == null) {
                return;
            }
            ResourceLocation soundId = FoodDefinitionManager.getSound(itemId.toString(), keyFrames.getKeyframeData().getSound());
            if (soundId != null) {
                Network.CHANNEL.sendToServer(new SoundPacket(itemId, soundId));
            }
            clientCameraShakeHandler.run();
        });
        controller.setCustomInstructionKeyframeHandler(keyFrames ->{
            if (keyFrames.getKeyframeData().getInstructions().contains("finished")){
                if (Food.temp != null) {
                    clientUseReequipHandler.accept(Food.temp.copy());
                    Food.temp = null;
                    //消耗物品
                    Network.CHANNEL.sendToServer(new FinishUsePacket(InteractionHand.MAIN_HAND));
                }
                controller.stop();
                Food.lockedHotbarSlot = -1;
            }
        });
        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}

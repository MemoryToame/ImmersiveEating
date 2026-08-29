package io.github.memorytoame.immersiveeating.neoforge.additions;

import io.github.memorytoame.immersiveeating.neoforge.additions.definiton.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.init.ModItems;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.CustomUsingPacket;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.FinishUsePacket;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.SoundPacket;
import io.github.memorytoame.immersiveeating.neoforge.unique.UniqueItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Empty extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static Consumer<ItemStack> clientUseReequipHandler = ignored -> {};
    private static Runnable clientCameraShakeHandler = () -> {};
    private static Supplier<ItemStack> clientAnimationStackSupplier = () -> ItemStack.EMPTY;
    private static Runnable clientAnimationStackClearHandler = () -> {};
    private static Runnable clientHotbarUnlockHandler = () -> {};
    private static Predicate<Item> clientMainHandItemChecker = item -> false;
    private static Supplier<Item> clientGetMainHandItem = () -> {return null;};


    public Empty(Properties pProperties) {
        super(pProperties);
        GeoItem.registerSyncedAnimatable(this);
    }

    public static void setClientHooks(Consumer<ItemStack> useReequipHandler, Runnable cameraShakeHandler, Supplier<ItemStack> animationStackSupplier, Runnable animationStackClearHandler, Runnable hotbarUnlockHandler, Supplier<Item> mainHandItem) {
        clientUseReequipHandler = useReequipHandler;
        clientCameraShakeHandler = cameraShakeHandler;
        clientAnimationStackSupplier = animationStackSupplier;
        clientAnimationStackClearHandler = animationStackClearHandler;
        clientHotbarUnlockHandler = hotbarUnlockHandler;
        clientGetMainHandItem = mainHandItem;
    }
    public static void setMainHandItemChecker(Predicate<Item> checker) {
        clientMainHandItemChecker = checker;
    }
    public static void stopEatAnimation() {
        Empty empty = ModItems.EMPTY.get();
        long instanceId = GeoItem.getId(clientAnimationStackSupplier.get());
        AnimationController<?> controller = empty.getAnimatableInstanceCache().getManagerForId(instanceId).getAnimationControllers().get("eat");
        if (controller != null) {
            controller.stop();
        }
    }
    public static boolean isEatAnimationPlaying() {
        Empty empty = ModItems.EMPTY.get();
        long instanceId = GeoItem.getId(clientAnimationStackSupplier.get());
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
            ItemStack animationStack = clientAnimationStackSupplier.get();
            if (animationStack == null || animationStack.isEmpty()) {
                return;
            }
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(animationStack.getItem());
            if (itemId == null) {
                return;
            }
            String soundKey = keyFrames.getKeyframeData().getSound();
            if ("pickup".equals(soundKey)) {
                PacketDistributor.sendToServer(new SoundPacket(itemId, ResourceLocation.fromNamespaceAndPath("minecraft", "entity.item.pickup")));
            }
            ResourceLocation soundId = FoodDefinitionManager.getSound(itemId.toString(), keyFrames.getKeyframeData().getSound());
            if (soundId !=null){
                if (clientGetMainHandItem.get() != null) {
                    ResourceLocation mainHandId = BuiltInRegistries.ITEM.getKey(clientGetMainHandItem.get());
                    ResourceLocation childId = UniqueItem.UNIQUE_ITEM_MAP.get(mainHandId);
                    if (childId != null) {
                        Item childItem = BuiltInRegistries.ITEM.get(childId);
                        if (childItem != null) {
                            PacketDistributor.sendToServer(new CustomUsingPacket(InteractionHand.MAIN_HAND, childItem, 1));
                        }
                    } else {
                        PacketDistributor.sendToServer(new SoundPacket(itemId, soundId));
                    }
                }
            }

            clientCameraShakeHandler.run();
        });
        controller.setCustomInstructionKeyframeHandler(keyFrames ->{
            if (keyFrames.getKeyframeData().getInstructions().contains("finished")){
                ItemStack animationStack = clientAnimationStackSupplier.get();
                controller.stop();
                if (animationStack != null && !animationStack.isEmpty()) {

                    clientAnimationStackClearHandler.run();
                    //消耗物品
                    PacketDistributor.sendToServer(new FinishUsePacket(InteractionHand.MAIN_HAND));
                    clientUseReequipHandler.accept(animationStack.copy());
                }
                clientHotbarUnlockHandler.run();
            }
        });
        controllers.add(controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}

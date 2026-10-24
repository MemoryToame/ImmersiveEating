package org.toame.food.additions;

import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.init.ModItems;
import org.toame.food.network.packet.CustomUsingPacket;
import org.toame.food.network.packet.FinishUsePacket;
import org.toame.food.network.Network;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.toame.food.unique.UniqueItem.UNIQUE_ITEM_MAP;

public class Empty extends Item implements GeoItem, GeoAnimatable {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static Consumer<ItemStack> clientUseReequipHandler = ignored -> {};
    private static Supplier<ItemStack> clientAnimationStackSupplier = () -> ItemStack.EMPTY;
    private static Runnable clientAnimationStackClearHandler = () -> {};
    private static Runnable clientHotbarUnlockHandler = () -> {};
    private static Supplier<Item> clientGetMainHandItem = () -> null;
    private static Supplier<UUID> clientAnimationSessionSupplier = () -> null;
    private static Supplier<Long> clientAnimationControllerSupplier = () -> Long.MAX_VALUE;
    private static Consumer<ResourceLocation> clientSoundHandler = ignored -> {};
    private static Runnable clientSoundStopHandler = () -> {};

    public Empty(Properties pProperties) {
        super(pProperties);
        GeoItem.registerSyncedAnimatable(this);
    }


    public static void setClientHooks(Consumer<ItemStack> useReequipHandler , Supplier<ItemStack> animationStackSupplier, Runnable animationStackClearHandler, Runnable hotbarUnlockHandler,Supplier<Item> mainHandItem, Supplier<UUID> animationSessionSupplier, Supplier<Long> animationControllerSupplier) {
        clientUseReequipHandler = useReequipHandler;
        clientAnimationStackSupplier = animationStackSupplier;
        clientAnimationStackClearHandler = animationStackClearHandler;
        clientHotbarUnlockHandler = hotbarUnlockHandler;
        clientGetMainHandItem= mainHandItem;
        clientAnimationSessionSupplier = animationSessionSupplier;
        clientAnimationControllerSupplier = animationControllerSupplier;
    }
    public static void setClientSoundHooks(Consumer<ResourceLocation> soundHandler, Runnable soundStopHandler) {
        clientSoundHandler = soundHandler;
        clientSoundStopHandler = soundStopHandler;
    }

    public static void stopEatAnimation(long instanceId) {
        clientSoundStopHandler.run();
        Empty empty = ModItems.EMPTY.get();
        AnimationController<?> controller = empty.getAnimatableInstanceCache().getManagerForId(instanceId).getAnimationControllers().get("eat");
        if (controller != null) {
            controller.stop();
        }
    }

    public static boolean isEatAnimationPlaying() {
        return isClientAnimationPlaying(clientAnimationStackSupplier.get(), "eat");
    }

    public static boolean isAnimationPlaying() {
        return isClientAnimationPlaying(clientAnimationStackSupplier.get(), "eat")||
                isClientAnimationPlaying(clientAnimationStackSupplier.get(), "switchin")||
                isClientAnimationPlaying(clientAnimationStackSupplier.get(), "switchout");
    }

    public static boolean isClientAnimationPlaying(ItemStack stack, String controllerName) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        long instanceId = resolveControllerId(stack);

        Empty empty = ModItems.EMPTY.get();
        AnimationController<?> controller = empty.getAnimatableInstanceCache().getManagerForId(instanceId).getAnimationControllers().get(controllerName);
        return controller != null && controller.isPlayingTriggeredAnimation();
    }

    public static void triggerClientAnimation(ItemStack stack, String controllerName) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        Empty empty = ModItems.EMPTY.get();
        long instanceId = resolveControllerId(stack);
        AnimationController<?> controller = empty.getAnimatableInstanceCache().getManagerForId(instanceId).getAnimationControllers().get(controllerName);

        if (controller != null) {
            clientSoundStopHandler.run();
            controller.forceAnimationReset();
            controller.tryTriggerAnimation(controllerName);
        }
    }

    private List<AnimationController<Empty>> reg_controllers = new ArrayList<>();
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        reg_controllers.add(createControl("eat"));
        reg_controllers.add(createControl("switchin"));
        reg_controllers.add(createControl("switchout"));
        for (AnimationController<Empty> controller : reg_controllers){
            controller.setSoundKeyframeHandler(keyFrames ->{
                ItemStack animationStack = clientAnimationStackSupplier.get();
                if (animationStack == null || animationStack.isEmpty()) {
                    return;
                }
                ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(animationStack.getItem());
                String soundKey = keyFrames.getKeyframeData().getSound();

                ResourceLocation soundId = FoodDefinitionManager.getSound(itemId.toString(), soundKey);
                if (soundId != null) {
                    //特殊物品用单独数据包
                    if (clientGetMainHandItem.get()!=null){
                        if (UNIQUE_ITEM_MAP.get(BuiltInRegistries.ITEM.getKey(clientGetMainHandItem.get()))!=null){
                            Item child_item =BuiltInRegistries.ITEM.get(UNIQUE_ITEM_MAP.get(BuiltInRegistries.ITEM.getKey(clientGetMainHandItem.get())));
                            if (child_item!=null){
                                Network.CHANNEL.sendToServer(new CustomUsingPacket(InteractionHand.MAIN_HAND, child_item,1));
                            }
                        }
                        else{
                            clientSoundHandler.accept(soundId);
                        }
                    }
                }
            });
            controller.setCustomInstructionKeyframeHandler(keyFrames ->{
                if (keyFrames.getKeyframeData().getInstructions().contains("finished")){
                    ItemStack animationStack = clientAnimationStackSupplier.get();
                    controller.stop();
                    if (animationStack != null && !animationStack.isEmpty()) {
                        clientAnimationStackClearHandler.run();
                        //消耗物品
                        UUID sessionId = clientAnimationSessionSupplier.get();
                        if (sessionId != null) {
                            Network.CHANNEL.sendToServer(new FinishUsePacket(sessionId, InteractionHand.MAIN_HAND));
                        }
                        clientUseReequipHandler.accept(animationStack.copy());
                    }
                    clientHotbarUnlockHandler.run();
                }
            });
            controllers.add(controller);
        }
    }

    private AnimationController<Empty> createControl(String name) {
        AnimationController<Empty> controller = new AnimationController<>(this, name, state -> PlayState.STOP);

        return controller.triggerableAnim(name, RawAnimation.begin().thenPlay(name));
    }

    private static long resolveControllerId(ItemStack stack) {
        return clientAnimationControllerSupplier.get();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getBoneResetTime() {
        return 0.0D;
    }
}

package io.github.memorytoame.immersiveeating.neoforge.common.content.item;

import io.github.memorytoame.immersiveeating.neoforge.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.neoforge.init.ModItems;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.CustomUsingPacket;
import io.github.memorytoame.immersiveeating.neoforge.network.packet.FinishUsePacket;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static io.github.memorytoame.immersiveeating.neoforge.unique.UniqueItem.UNIQUE_ITEM_MAP;

public class Empty extends Item implements GeoItem {


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

    public static void setClientHooks(Consumer<ItemStack> useReequipHandler, Supplier<ItemStack> animationStackSupplier, Runnable animationStackClearHandler, Runnable hotbarUnlockHandler,Supplier<Item> mainHandItem, Supplier<UUID> animationSessionSupplier, Supplier<Long> animationControllerSupplier) {
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
//                if ("pickup".equals(soundKey)) {
//                    clientSoundHandler.accept(ResourceLocation.fromNamespaceAndPath("minecraft", "entity.item.pickup"));
//                }
                ResourceLocation soundId = FoodDefinitionManager.getSound(itemId.toString(), soundKey);
                if (soundId != null) {
                    //特殊物品用单独数据包
                    if (clientGetMainHandItem.get()!=null){
                        if (UNIQUE_ITEM_MAP.get(BuiltInRegistries.ITEM.getKey(clientGetMainHandItem.get()))!=null){
                            Item child_item =BuiltInRegistries.ITEM.get(UNIQUE_ITEM_MAP.get(BuiltInRegistries.ITEM.getKey(clientGetMainHandItem.get())));
                            if (child_item!=null){
                                PacketDistributor.sendToServer(new CustomUsingPacket(InteractionHand.MAIN_HAND, child_item,1));
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
                            PacketDistributor.sendToServer(new FinishUsePacket(InteractionHand.MAIN_HAND, sessionId));
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
        long stackId = stack == null || stack.isEmpty() ? Long.MAX_VALUE : GeoItem.getId(stack);
        return stackId == Long.MAX_VALUE ? clientAnimationControllerSupplier.get() : stackId;
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

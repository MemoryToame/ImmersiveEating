package org.toame.food.additions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.network.Network;
import org.toame.food.network.packet.CustomUsingPacket;
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
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.toame.food.unique.UniqueItem.*;

public class Empty extends Item implements GeoItem {


    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack stack;
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

    public static void setClientHooks(Consumer<ItemStack> useReequipHandler, Runnable cameraShakeHandler, Supplier<ItemStack> animationStackSupplier, Runnable animationStackClearHandler, Runnable hotbarUnlockHandler,Supplier<Item> mainHandItem) {
        clientUseReequipHandler = useReequipHandler;
        clientCameraShakeHandler = cameraShakeHandler;
        clientAnimationStackSupplier = animationStackSupplier;
        clientAnimationStackClearHandler = animationStackClearHandler;
        clientHotbarUnlockHandler = hotbarUnlockHandler;
        clientGetMainHandItem= mainHandItem;
    }
    public static void setMainHandItemChecker(Predicate<Item> checker) {
        clientMainHandItemChecker = checker;
    }

    public ItemStack getRenderStack() {
        if (stack == null) {
            stack = new ItemStack(this);
        }
        return stack;
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
        AnimationController<?> controller = empty.getAnimatableInstanceCache().getManagerForId(instanceId).getAnimationControllers().get("eat");
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
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(animationStack.getItem());
            if (itemId == null) {
                return;
            }
            String soundKey = keyFrames.getKeyframeData().getSound();
            if ("pickup".equals(soundKey)) {
                Network.CHANNEL.sendToServer(new SoundPacket(itemId, new ResourceLocation("minecraft", "entity.item.pickup")));
            }
            ResourceLocation soundId = FoodDefinitionManager.getSound(itemId.toString(), soundKey);
            if (soundId != null) {

                //特殊物品用单独数据包
                if (clientGetMainHandItem.get()!=null){
                    if (UNIQUE_ITEM_MAP.get(ForgeRegistries.ITEMS.getKey(clientGetMainHandItem.get()))!=null){
                        Item child_item = ForgeRegistries.ITEMS.getValue(UNIQUE_ITEM_MAP.get(ForgeRegistries.ITEMS.getKey(clientGetMainHandItem.get())));
                        if (child_item!=null){
                            Network.CHANNEL.sendToServer(new CustomUsingPacket(InteractionHand.MAIN_HAND, child_item,1));
                        }
                    }
                    else{
                        Network.CHANNEL.sendToServer(new SoundPacket(itemId, soundId));
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
                    Network.CHANNEL.sendToServer(new FinishUsePacket(InteractionHand.MAIN_HAND));
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

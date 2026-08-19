package org.toame.food.additions;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.toame.food.Food;
import org.toame.food.additions.definiton.FoodDefinitionManager;
import org.toame.food.mixin.Accessor.ItemInHandRendererAccessor;
import org.toame.food.network.Network;
import org.toame.food.network.packet.SoundPacket;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraftforge.registries.ForgeRegistries;

public class Empty extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private ItemStack stack;

    public Empty(Properties pProperties) {
        super(pProperties);
        GeoItem.registerSyncedAnimatable(this);
    }
    public ItemStack getRenderStack() {
        if (stack == null) {
            stack = new ItemStack(this);
        }
        return stack;
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
        });
        controller.setCustomInstructionKeyframeHandler(keyFrames ->{
            if (keyFrames.getKeyframeData().getInstructions().contains("finished")){
                if (Food.temp != null) {
                    ItemInHandRendererAccessor itemInHandRenderer = (ItemInHandRendererAccessor) Minecraft.getInstance().gameRenderer.itemInHandRenderer;
                    ItemStack fakeStack = Food.temp.copy();
                    fakeStack.setCount(Food.temp.getCount() + 1);
                    itemInHandRenderer.setMainHandItem(fakeStack);
                    Food.temp = null;
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

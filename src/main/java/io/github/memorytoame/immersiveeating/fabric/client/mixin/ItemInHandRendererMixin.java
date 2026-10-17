package io.github.memorytoame.immersiveeating.fabric.client.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.memorytoame.immersiveeating.fabric.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.fabric.common.content.item.Empty;
import io.github.memorytoame.immersiveeating.fabric.utils.AnimationUtils;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
   @Shadow
   private ItemStack mainHandItem;
   @Shadow
   private float mainHandHeight;
   @Shadow
   private float oMainHandHeight;
   @Unique
   private ItemStack food$selectedMainHandItem = ItemStack.EMPTY;
   @Unique
   private ItemStack food$renderedMainHandItem = ItemStack.EMPTY;
   @Unique
   private ItemStack food$pendingMainHandItem = ItemStack.EMPTY;
   @Unique
   private ItemStack food$switchOutItem = ItemStack.EMPTY;
   @Unique
   private boolean food$hasTrackedSelectedItem;
   @Unique
   private boolean food$hasTrackedRenderedItem;
   @Unique
   private boolean food$waitingForSwitchOut;
   @Unique
   private boolean food$hasTrackedCustomMainHand;
   @Unique
   private boolean food$customMainHand;

   @Shadow
   public abstract void itemUsed(InteractionHand var1);

   @Inject(method = "tick", at = @At("HEAD"))
   private void food$prepareOffHandEquipAnimation(CallbackInfo ci) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         this.food$hasTrackedCustomMainHand = false;
         this.food$customMainHand = false;
      } else {
         boolean customMainHand = FoodDefinitionManager.init_ItemList.contains(mc.player.getMainHandItem().getItem());
         if (this.food$hasTrackedCustomMainHand && this.food$customMainHand && !customMainHand) {
            this.itemUsed(InteractionHand.OFF_HAND);
         }

         this.food$customMainHand = customMainHand;
         this.food$hasTrackedCustomMainHand = true;
      }
   }

   @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
   private void food$hideOffHandForCustomMainHand(
      AbstractClientPlayer player,
      float partialTick,
      float pitch,
      InteractionHand hand,
      float swingProgress,
      ItemStack stack,
      float equipProgress,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int packedLight,
      CallbackInfo ci
   ) {
      if (hand == InteractionHand.OFF_HAND && this.food$isCustomMainHandBeingRendered(player)) {
         ci.cancel();
      }
   }

   @Unique
   private boolean food$isCustomMainHandBeingRendered(AbstractClientPlayer player) {
      if (this.food$waitingForSwitchOut) {
         return true;
      } else {
         return FoodDefinitionManager.init_ItemList.contains(player.getMainHandItem().getItem()) || !this.food$renderedMainHandItem.isEmpty() && FoodDefinitionManager.init_ItemList.contains(this.food$renderedMainHandItem.getItem());
      }
   }

   @Inject(method = "tick", at = @At("HEAD"))
   private void food$triggerSwitchOut(CallbackInfo ci) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         this.food$resetTracking();
      } else {
         ItemStack mainHandItem = mc.player.getMainHandItem();
         if (!this.food$hasTrackedSelectedItem) {
            this.food$selectedMainHandItem = mainHandItem.copy();
            this.food$hasTrackedSelectedItem = true;
         } else {
            if (!ItemStack.isSameItemSameComponents(this.food$selectedMainHandItem, mainHandItem)) {
               this.food$selectedMainHandItem = mainHandItem.copy();
               if (!this.food$waitingForSwitchOut && food$trigger(this.food$renderedMainHandItem, "switchout")) {
                  this.food$switchOutItem = this.food$renderedMainHandItem.copy();
                  this.food$pendingMainHandItem = mainHandItem.copy();
                  this.food$waitingForSwitchOut = true;
               }
            }
         }
      }
   }

   @Inject(method = "tick", at = @At("TAIL"))
   private void food$triggerSwitchIn(CallbackInfo ci) {
      if (this.food$hasTrackedSelectedItem) {
         if (this.food$waitingForSwitchOut) {
            if (Empty.isClientAnimationPlaying(this.food$switchOutItem, "switchout")) {
               this.mainHandItem = this.food$switchOutItem.copy();
               this.mainHandHeight = 1.0F;
               this.oMainHandHeight = 1.0F;
            } else {
               this.mainHandItem = this.food$pendingMainHandItem.copy();
               this.mainHandHeight = 0.0F;
               this.oMainHandHeight = 0.0F;
               this.food$renderedMainHandItem = this.mainHandItem.copy();
               this.food$waitingForSwitchOut = false;
               this.food$switchOutItem = ItemStack.EMPTY;
               this.food$pendingMainHandItem = ItemStack.EMPTY;
               food$trigger(this.mainHandItem, "switchin");
            }
         } else if (!this.food$hasTrackedRenderedItem) {
            this.food$renderedMainHandItem = this.mainHandItem.copy();
            this.food$hasTrackedRenderedItem = true;
         } else {
            if (!ItemStack.isSameItemSameComponents(this.food$renderedMainHandItem, this.mainHandItem)) {
               food$trigger(this.mainHandItem, "switchin");
               this.food$renderedMainHandItem = this.mainHandItem.copy();
            }
         }
      }
   }

   @Unique
   private static boolean food$trigger(ItemStack stack, String controllerName) {
      if (!stack.isEmpty() && FoodDefinitionManager.init_ItemList.contains(stack.getItem()) && food$hasAnimation(stack, controllerName)) {
         if ("switchin".equals(controllerName)) {
            AnimationUtils.beginSwitchInAnimation(stack);
         }
         Empty.triggerClientAnimation(stack, controllerName);
         return true;
      } else {
         return false;
      }
   }

   @Unique
   private static boolean food$hasAnimation(ItemStack stack, String animationName) {
      if (animationName == null || animationName.isBlank()) {
         return false;
      }

      ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
      String animationId = "sakura_fubuki".equals(itemId.getPath()) ? "flower_tea" : itemId.getPath();
      ResourceLocation animationResource = ResourceLocation.fromNamespaceAndPath("food", "animations/" + animationId + ".animation.json");
      return Minecraft.getInstance().getResourceManager().getResource(animationResource).map(resource -> {
         try (Reader reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
               return false;
            }

            JsonObject animations = root.getAsJsonObject().getAsJsonObject("animations");
            return animations != null && animations.has(animationName);
         } catch (Exception ignored) {
            return false;
         }
      }).orElse(false);
   }

   @Unique
   private void food$resetTracking() {
      this.food$selectedMainHandItem = ItemStack.EMPTY;
      this.food$renderedMainHandItem = ItemStack.EMPTY;
      this.food$pendingMainHandItem = ItemStack.EMPTY;
      this.food$switchOutItem = ItemStack.EMPTY;
      this.food$hasTrackedSelectedItem = false;
      this.food$hasTrackedRenderedItem = false;
      this.food$waitingForSwitchOut = false;
   }
}


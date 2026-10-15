package io.github.memorytoame.immersiveeating.fabric.client.render;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Matrix4f;

public enum ArmModelOffset {
   WIDE(new Matrix4f(), new Matrix4f()),
   SLIM(new Matrix4f(), new Matrix4f().translation(-0.0625F, 0.0F, 0.0F));

   private final Matrix4f leftHand;
   private final Matrix4f rightHand;
   private static final Set<String> UNIQUE_ITEMS = Set.of(
      "kaleidoscope_cookery:cold_style_sashimi",
      "kaleidoscope_cookery:desert_style_sashimi",
      "kaleidoscope_cookery:nether_style_sashimi",
      "kaleidoscope_cookery:tundra_style_sashimi",
      "kaleidoscope_cookery:end_style_sashimi"
   );

   ArmModelOffset(Matrix4f leftHand, Matrix4f rightHand) {
      this.leftHand = leftHand;
      this.rightHand = rightHand;
   }

   public static ArmModelOffset forPlayer(AbstractClientPlayer player) {
      return player != null && "slim".equals(player.getSkin().model().id()) ? SLIM : WIDE;
   }

   public Matrix4f matrixFor(HumanoidArm arm) {
      return !isHoldingOffsetItem() ? new Matrix4f() : new Matrix4f(arm == HumanoidArm.RIGHT ? this.rightHand : this.leftHand);
   }

   private static boolean isHoldingOffsetItem() {
      AbstractClientPlayer player = Minecraft.getInstance().player;
      if (player == null) {
         return false;
      }

      ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem());
      return UNIQUE_ITEMS.contains(itemId.toString());
   }
}


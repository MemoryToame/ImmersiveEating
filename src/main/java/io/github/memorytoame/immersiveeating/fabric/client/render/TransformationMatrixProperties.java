package io.github.memorytoame.immersiveeating.fabric.client.render;

public enum TransformationMatrixProperties {
   NORMAL(-1.05F, -0.35F, -0.8F, 0.55F);

   private final float translateX;
   private final float translateY;
   private final float translateZ;
   private final float scale;

   TransformationMatrixProperties(float translateX, float translateY, float translateZ, float scale) {
      this.translateX = translateX;
      this.translateY = translateY;
      this.translateZ = translateZ;
      this.scale = scale;
   }

   public float getTranslateX() {
      return this.translateX;
   }

   public float getTranslateY() {
      return this.translateY;
   }

   public float getTranslateZ() {
      return this.translateZ;
   }

   public float getScale() {
      return this.scale;
   }
}


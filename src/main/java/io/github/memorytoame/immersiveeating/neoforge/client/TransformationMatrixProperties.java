package io.github.memorytoame.immersiveeating.neoforge.client;

public enum TransformationMatrixProperties {
    //NORMAL(-1.5F,0.05F,-0.3F,0.55F),
    NORMAL(-1.05F,-0.35F,-0.8F,0.55F);
//    EATING(-0.15F,0.1F,-0.3F,0.55F),
//    GOLDEN_SALAD(gpx,gpy,gpz,0.55F);
    private float translateX;
    private float translateY;
    private float translateZ;
    private float scale;
    TransformationMatrixProperties(float translateX, float translateY, float translateZ, float scale){
         this.translateX = translateX;
         this.translateY = translateY;
         this.translateZ = translateZ;
         this.scale = scale;
    }

    public float getTranslateX() {
        return translateX;
    }

    public float getTranslateY() {
        return translateY;
    }

    public float getTranslateZ() {
        return translateZ;
    }

    public float getScale() {
        return scale;
    }
}

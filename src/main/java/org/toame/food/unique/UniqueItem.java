package org.toame.food.unique;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class UniqueItem {
    public static final Map<ResourceLocation,ResourceLocation> UNIQUE_ITEM_MAP = new HashMap<>();
    public static final ResourceLocation BAOZI_PLATE_ID = new ResourceLocation("kaleidoscope_cookery", "baozi_plate");
    public static final ResourceLocation BAOZI_ID = new ResourceLocation("kaleidoscope_cookery", "baozi");
    public static final ResourceLocation QINGTUAN_PLATE_ID = new ResourceLocation("kaleidoscope_cookery", "qingtuan_plate");
    public static final ResourceLocation QINGTUAN_ID = new ResourceLocation("kaleidoscope_cookery", "qingtuan");

    public static void initUniqueItemMap(){
        UNIQUE_ITEM_MAP.put(BAOZI_PLATE_ID,BAOZI_ID);
        UNIQUE_ITEM_MAP.put(QINGTUAN_PLATE_ID,QINGTUAN_ID);
    }
}

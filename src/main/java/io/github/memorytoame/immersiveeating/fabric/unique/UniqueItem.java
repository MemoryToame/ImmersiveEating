package io.github.memorytoame.immersiveeating.fabric.unique;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class UniqueItem {
   public static final Map<ResourceLocation, ResourceLocation> UNIQUE_ITEM_MAP = new HashMap<>(4);
   public static final ResourceLocation BAOZI_PLATE_ID = ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "baozi_plate");
   public static final ResourceLocation BAOZI_ID = ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "baozi");
   public static final ResourceLocation QINGTUAN_PLATE_ID = ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "qingtuan_plate");
   public static final ResourceLocation QINGTUAN_ID = ResourceLocation.fromNamespaceAndPath("kaleidoscope_cookery", "qingtuan");

   public static void initUniqueItemMap() {
      UNIQUE_ITEM_MAP.put(BAOZI_PLATE_ID, BAOZI_ID);
      UNIQUE_ITEM_MAP.put(QINGTUAN_PLATE_ID, QINGTUAN_ID);
   }
}


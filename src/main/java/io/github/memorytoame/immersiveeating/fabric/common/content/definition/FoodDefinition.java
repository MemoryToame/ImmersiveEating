package io.github.memorytoame.immersiveeating.fabric.common.content.definition;

import java.util.List;
import java.util.Map;

public class FoodDefinition {
   private String item;
   private Map<String, String> sounds;
   private List<String> invisible;

   public String getItem() {
      return this.item;
   }

   public Map<String, String> getSounds() {
      return this.sounds;
   }

   public List<String> getInvisible() {
      return this.invisible;
   }
}


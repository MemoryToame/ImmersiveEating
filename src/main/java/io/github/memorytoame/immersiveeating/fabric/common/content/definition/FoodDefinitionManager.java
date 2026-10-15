package io.github.memorytoame.immersiveeating.fabric.common.content.definition;

import com.google.gson.Gson;
import io.github.memorytoame.immersiveeating.fabric.KaleidoscopeImmersiveEating;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public final class FoodDefinitionManager implements SimpleSynchronousResourceReloadListener {
   private static final Gson GSON = new Gson();
   public static final List<FoodDefinition> list = new ArrayList<>(32);
   public static final List<String> init_ItemIdList = new ArrayList<>(32);
   public static final List<Item> init_ItemList = new ArrayList<>(32);
   private static final Map<String, Map<String, String>> soundMappings = new HashMap<>(32);
   private static final Map<String, Set<String>> invisibleMappings = new HashMap<>(32);

   @Override
   public ResourceLocation getFabricId() {
      return ResourceLocation.fromNamespaceAndPath(KaleidoscopeImmersiveEating.MOD_ID, "food_definitions");
   }

   @Override
   public void onResourceManagerReload(ResourceManager resourceManager) {
      list.clear();
      soundMappings.clear();
      invisibleMappings.clear();
      init_ItemList.clear();
      init_ItemIdList.clear();
      Map<ResourceLocation, Resource> resources = resourceManager.listResources(
         "definitions",
         id -> id.getNamespace().equals(KaleidoscopeImmersiveEating.MOD_ID) && id.getPath().endsWith(".json")
      );
      for (Resource resource : resources.values()) {
         try (Reader reader = resource.openAsReader()) {
            FoodDefinition definition = GSON.fromJson(reader, FoodDefinition.class);
            if (definition == null || definition.getItem() == null) {
               continue;
            }
            list.add(definition);
            ResourceLocation itemId = ResourceLocation.tryParse(definition.getItem());
            Item item = itemId == null ? null : BuiltInRegistries.ITEM.get(itemId);
            if (item != null && item != Items.AIR) {
               init_ItemIdList.add(itemId.toString());
               init_ItemList.add(item);
            }
            if (definition.getSounds() != null) {
               soundMappings.computeIfAbsent(definition.getItem(), ignored -> new HashMap<>(8)).putAll(definition.getSounds());
            }
            if (definition.getInvisible() != null && !definition.getInvisible().isEmpty()) {
               Set<String> invisibleBones = invisibleMappings.computeIfAbsent(definition.getItem(), ignored -> new HashSet<>(4));
               for (String boneName : definition.getInvisible()) {
                  if (boneName != null && !boneName.isBlank()) {
                     invisibleBones.add(boneName.toLowerCase());
                  }
               }
            }
         } catch (Exception exception) {
            KaleidoscopeImmersiveEating.LOGGER.warn("Failed to load food definition {}", resource, exception);
         }
      }
   }

   public static ResourceLocation getSound(String itemId, String keyframeName) {
      Map<String, String> itemSounds = soundMappings.get(itemId);
      if (itemSounds == null) {
         return null;
      }
      String soundId = itemSounds.get(keyframeName);
      return soundId != null && !soundId.isBlank() ? ResourceLocation.tryParse(soundId) : null;
   }

   public static boolean isInvisible(String itemId, String boneName) {
      Set<String> invisibleBones = invisibleMappings.get(itemId);
      return invisibleBones != null && boneName != null && invisibleBones.contains(boneName.toLowerCase());
   }
}


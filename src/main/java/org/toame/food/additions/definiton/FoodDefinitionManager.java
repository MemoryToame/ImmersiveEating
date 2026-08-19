package org.toame.food.additions.definiton;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.toame.food.additions.CustomRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FoodDefinitionManager extends SimpleJsonResourceReloadListener {


    private static final Gson GSON = new Gson();

    public FoodDefinitionManager() {
        super(GSON, "definitions");
    }
    public static final List<FoodDefinition> list = new ArrayList();
    private static final Map<String, Map<String, String>> soundMappings = new HashMap<>();
    private static final Map<String, Set<String>> invisibleMappings = new HashMap<>();

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        list.clear();
        soundMappings.clear();
        invisibleMappings.clear();
        CustomRenderer.init_ItemList.clear();
        CustomRenderer.init_ItemIdList.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : pObject.entrySet()) {
            FoodDefinition definition = GSON.fromJson(entry.getValue(), FoodDefinition.class);
            list.add(definition);
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(definition.getItem()));
            if (item != null) {
                CustomRenderer.init_ItemIdList.add(definition.getItem());
                CustomRenderer.init_ItemList.add(item);
            }
            if (definition.getSounds() != null) {
                soundMappings.computeIfAbsent(definition.getItem(), ignored -> new HashMap<>()).putAll(definition.getSounds());
            }
            if (definition.getInvisible() != null && !definition.getInvisible().isEmpty()) {
                Set<String> invisibleBones = invisibleMappings.computeIfAbsent(definition.getItem(), ignored -> new HashSet<>());
                for (String boneName : definition.getInvisible()) {
                    if (boneName != null && !boneName.isBlank()) {
                        invisibleBones.add(boneName.toLowerCase());
                    }
                }
            }
        }
    }

    public static ResourceLocation getSound(String itemId, String keyframeName) {
        Map<String, String> itemSounds = soundMappings.get(itemId);
        if (itemSounds == null) {
            return null;
        }
        String soundId = itemSounds.get(keyframeName);
        return soundId == null || soundId.isBlank() ? null : ResourceLocation.tryParse(soundId);
    }

    public static boolean isInvisible(String itemId, String boneName) {
        Set<String> invisibleBones = invisibleMappings.get(itemId);
        return invisibleBones != null && boneName != null && invisibleBones.contains(boneName.toLowerCase());
    }
}

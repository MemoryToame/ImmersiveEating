package org.toame.food.additions.sound;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.toame.food.additions.definiton.FoodDefinition;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SoundJson {
    private static final Gson GSON = new Gson();

    public static JsonObject generate(List<FoodDefinition> definitions) {
        JsonObject root = new JsonObject();

        for (FoodDefinition definition : definitions) {
            if (definition.getSounds() == null) {
                continue;
            }

            for (String sound_namespace : definition.getSounds().values()) {
                if (sound_namespace == null || sound_namespace.isBlank()) {
                    continue;
                }

                ResourceLocation soundId = ResourceLocation.tryParse(sound_namespace);
                if (soundId == null) {
                    continue;
                }
                String name = soundId.getPath(); //如果是eat:eating则为eating

                addSound(root, name, soundId.toString());
            }
        }

        return root;
    }

    public static JsonObject generate(ResourceManager resourceManager) {
        List<FoodDefinition> definitions = new ArrayList<>();
        Map<ResourceLocation, Resource> resources = resourceManager.listResources("definitions",
                location -> location.getNamespace().equals("food") && location.getPath().endsWith(".json"));

        for (Resource resource : resources.values()) {
            try (Reader reader = resource.openAsReader()) {
                FoodDefinition definition = GSON.fromJson(reader, FoodDefinition.class);
                if (definition != null) {
                    definitions.add(definition);
                }
            } catch (IOException exception) {
                throw new RuntimeException("Failed to read food definition", exception);
            }
        }

        return generate(definitions);
    }
    private static void addSound(JsonObject root, String name, String soundId) {
        if (root.has(name)) {
            return;
        }

        JsonObject sound = new JsonObject();
        JsonArray sounds = new JsonArray();
        sounds.add(soundId);
        sound.add("sounds", sounds);
        root.add(name, sound);
    }

}

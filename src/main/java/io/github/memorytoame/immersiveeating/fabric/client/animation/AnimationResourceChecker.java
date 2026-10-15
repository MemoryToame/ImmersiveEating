package io.github.memorytoame.immersiveeating.fabric.client.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;


@SuppressWarnings("DuplicatedCode")
public final class AnimationResourceChecker {
    public static boolean hasAnimation(ItemStack stack, String animationName) {
        if (stack != null && !stack.isEmpty() && animationName != null && !animationName.isBlank()) {
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
        } else {
            return false;
        }
    }
}

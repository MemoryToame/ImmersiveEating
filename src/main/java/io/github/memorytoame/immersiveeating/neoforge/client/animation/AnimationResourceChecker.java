package io.github.memorytoame.immersiveeating.neoforge.client.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.memorytoame.immersiveeating.neoforge.Food;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
/*
此类针对于部分特殊食物没有animation.json存在
 */
public final class AnimationResourceChecker {
    private AnimationResourceChecker() {}
    public static boolean hasAnimation(ItemStack stack, String animationName) {
        if (stack == null || stack.isEmpty() || animationName == null || animationName.isBlank()) {
            return false;
        }
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String animationId = "sakura_fubuki".equals(itemId.getPath()) ? "flower_tea" : itemId.getPath();
        ResourceLocation animationResource = ResourceLocation.fromNamespaceAndPath(Food.MODID, "animations/" + animationId + ".animation.json");
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
    }
}


package io.github.memorytoame.immersiveeating.neoforge.additions.sound;

import com.google.gson.JsonObject;
import io.github.memorytoame.immersiveeating.neoforge.Food;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class FoodDynamicPack implements PackResources {

    private static final PackMetadataSection METADATA = new PackMetadataSection(Component.literal("Provides models and animations."), 34);
    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.CLIENT_RESOURCES) {
            return null;
        }
        if (!location.getNamespace().equals(Food.MODID)) {
            return null;
        }
        if (!location.getPath().equals("sounds.json")) {
            return null;
        }
        String content = createSoundsJson().toString();

        return () -> new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type == PackType.CLIENT_RESOURCES) {
            return Set.of(Food.MODID);
        }
        return Set.of();
    }

    @Override
    public void listResources(PackType type, String namespace, String prefix, ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES) {
            return;
        }
        if (!namespace.equals(Food.MODID)) {
            return;
        }
        if ("sounds.json".startsWith(prefix)) {
            output.accept(ResourceLocation.fromNamespaceAndPath(Food.MODID, "sounds.json"),
                    () -> {
                        JsonObject json = createSoundsJson();
                        return new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8)
                        );
                    }
            );
        }
    }

    @Override
    public IoSupplier<InputStream> getRootResource(String... path) {
        return null;
    }

    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        if (serializer == PackMetadataSection.TYPE) {
            return (T) METADATA;
        }
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return null;
    }

    @Override
    public String packId() {
        return "food_dynamic";
    }
    private static JsonObject createSoundsJson() {
        return SoundJson.generate(Minecraft.getInstance().getResourceManager());
    }
    @Override
    public void close() {
    }
}

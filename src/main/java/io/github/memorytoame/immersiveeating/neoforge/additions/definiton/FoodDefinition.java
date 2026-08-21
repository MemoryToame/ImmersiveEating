package io.github.memorytoame.immersiveeating.neoforge.additions.definiton;

import java.util.List;
import java.util.Map;

public class FoodDefinition {
    private String item;

    private Map<String, String> sounds;
    private List<String> invisible;

    public String getItem() {
        return item;
    }

    public Map<String, String> getSounds() {
        return sounds;
    }

    public List<String> getInvisible() {
        return invisible;
    }
}

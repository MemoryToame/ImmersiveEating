package org.toame.food.compat;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.toame.food.additions.definiton.FoodDefinitionManager;

import static com.github.ysbbbbbb.kaleidoscopecookery.KaleidoscopeCookery.LOGGER;

public class Helper {
    public static Class findApi(String className) {
        try {

            Class<?> api = Class.forName(className);
            return api;

        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.warn(className +" doesn't exist.");
            return null;
        }
    }
    public static boolean isCustomItem(ItemStack stack) {
        return !stack.isEmpty() && FoodDefinitionManager.init_ItemList.contains(stack.getItem());
    }
}

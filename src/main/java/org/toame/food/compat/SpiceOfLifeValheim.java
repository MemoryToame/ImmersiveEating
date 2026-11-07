package org.toame.food.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

public class SpiceOfLifeValheim {
    public static boolean enable(Player player, ItemStack itemStack) {
        try {
            Class<?> api = Helper.findApi("vice.sol_valheim.accessors.PlayerEntityMixinDataAccessor");
            if (api != null) {
                Object accessor = api.cast(player);
                Object foodData = api.getMethod("sol_valheim$getFoodData").invoke(accessor);
                if (foodData == null) return false;

                boolean eaten = false;

                // 1. Try RainLasea's 2-parameter version (ItemStack, LivingEntity)
                try {
                    Method eatMethod = foodData.getClass().getMethod("eatItem", ItemStack.class, LivingEntity.class);
                    Object result = eatMethod.invoke(foodData, itemStack, player);
                    if (result instanceof Boolean b) {
                        eaten = b;
                    }
                } catch (NoSuchMethodException e) {
                    // 2. Fallback: Try 1-parameter version (ItemStack)
                    try {
                        Method eatMethod = foodData.getClass().getMethod("eatItem", ItemStack.class);
                        Object result = eatMethod.invoke(foodData, itemStack);
                        if (result instanceof Boolean b) {
                            eaten = b;
                        } else {
                            eaten = true;
                        }
                    } catch (NoSuchMethodException e2) {
                        return false;
                    }
                }

                if (eaten) {
                    // Sync HUD data with client
                    try {
                        api.getMethod("sol_valheim$syncFoodData").invoke(accessor);
                    } catch (Exception ignored) {}

                    // Consume the item
                    itemStack.shrink(1);
                    return true;
                }
            }
            return false;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
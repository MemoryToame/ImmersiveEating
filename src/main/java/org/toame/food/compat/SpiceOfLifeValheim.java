package org.toame.food.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SpiceOfLifeValheim {
    public static boolean enable(Player player, ItemStack itemStack){
        try {
            Class api = Helper.findApi("vice.sol_valheim.accessors.PlayerEntityMixinDataAccessor");
            if (api!=null){
                Object accessor = api.cast(player);
                Object foodData = api.getMethod("sol_valheim$getFoodData").invoke(accessor);
                foodData.getClass().getMethod("eatItem", ItemStack.class).invoke(foodData, itemStack);
                return true;
            }
            return false;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}

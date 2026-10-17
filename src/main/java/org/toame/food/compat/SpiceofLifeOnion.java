package org.toame.food.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SpiceofLifeOnion {
    public static boolean enable(Player player, ItemStack itemStack){
        try {
            Class<?> solOnionClass = Helper.findApi("team.creative.solonion.common.SOLOnion");
//            Class api = Helper.findApi("team.creative.solonion.common.event.SOLOnionEvent");
            if (solOnionClass!=null){
                Object event  = solOnionClass.getField("EVENT").get(null);
                Method eat = event.getClass().getMethod("eat", ItemStack.class, Player.class);
                eat.invoke(event, itemStack, player);
                return true;
            }
            return false;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}

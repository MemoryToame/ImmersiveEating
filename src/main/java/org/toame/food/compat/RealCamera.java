package org.toame.food.compat;

import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

public class RealCamera {

    public static void disableRealCamera() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
        Class realCamera = Helper.findApi( "com.xtracr.realcamera.compat.DisableHelper");
        if (realCamera !=null){
            Field mainFeatureField = realCamera.getField("MAIN_FEATURE");
            Object mainFeature = mainFeatureField.get(null);

            Method registerMethod = null;

            for (Method method : mainFeature.getClass().getMethods()) {
                if (method.getName().equals("registerOr") && method.getParameterCount() == 1) {
                    registerMethod = method;
                    break;
                }
            }
            if (registerMethod == null) {
                return;
            }
            Predicate<Object> condition = player -> Helper.isCustomItem((((Player)player).getMainHandItem()));

            registerMethod.invoke(mainFeature, condition);
        }
    }
}

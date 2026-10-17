package io.github.memorytoame.immersiveeating.neoforge.compat;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.memorytoame.immersiveeating.neoforge.compat.Helper.isCustomItem;

public class Punchy {
    public static void disablePunchyForCustomItem(CallbackInfoReturnable<Boolean> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        /*
        1.是模组自定义的物品
        2.不在使用
         */
        if (isCustomItem(mc.player.getMainHandItem())&&!mc.player.isUsingItem()) {
            cir.setReturnValue(false);
        }
    }
}

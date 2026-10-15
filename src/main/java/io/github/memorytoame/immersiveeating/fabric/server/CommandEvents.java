package io.github.memorytoame.immersiveeating.fabric.server;

import com.mojang.brigadier.arguments.BoolArgumentType;
import io.github.memorytoame.immersiveeating.fabric.common.config.Config;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public final class CommandEvents {
   private CommandEvents() {
   }

   public static void register() {
      CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
         dispatcher.register(Commands.literal("immeat").requires(source -> source.hasPermission(2))
            .then(Commands.literal("config")
               .then(Commands.literal("enableRightClickAnimation")
                  .then(Commands.argument("value", BoolArgumentType.bool()).executes(context -> {
                     boolean enabled = BoolArgumentType.getBool(context, "value");
                     Config.ENABLE_RIGHT_BUTTON.set(enabled);
                     Config.COMMON_SPEC.save();
                     context.getSource().sendSuccess(() -> Component.literal("enableRightClickAnimation = " + enabled), true);
                     return 1;
                  })))))
      );
   }
}


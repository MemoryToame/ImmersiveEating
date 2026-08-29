package io.github.memorytoame.immersiveeating.neoforge.events;

import com.mojang.brigadier.arguments.BoolArgumentType;
import io.github.memorytoame.immersiveeating.neoforge.Food;
import io.github.memorytoame.immersiveeating.neoforge.config.Config;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Food.MODID)
public class CommandEvents {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("immeat").requires(source -> source.hasPermission(2)).then(
                Commands.literal("config")
                        .then(Commands.literal("enableRightClickAnimation")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(context -> {
                                            boolean enabled = BoolArgumentType.getBool(context, "value");
                                            Config.ENABLE_RIGHT_BUTTON.set(enabled);
                                            Config.COMMON_SPEC.save();
                                            context.getSource().sendSuccess(() -> Component.literal("enableRightClickAnimation = " + enabled), true);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }
}

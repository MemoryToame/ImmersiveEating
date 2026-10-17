package io.github.memorytoame.immersiveeating.fabric.client;

import io.github.memorytoame.immersiveeating.fabric.client.animation.CameraShake;
import io.github.memorytoame.immersiveeating.fabric.client.events.ClientReloadEvents;
import io.github.memorytoame.immersiveeating.fabric.client.events.FirstPersonModEvents;
import io.github.memorytoame.immersiveeating.fabric.client.network.ClientNetwork;
import io.github.memorytoame.immersiveeating.fabric.common.content.definition.FoodDefinitionManager;
import io.github.memorytoame.immersiveeating.fabric.common.content.item.Empty;
import io.github.memorytoame.immersiveeating.fabric.utils.AnimationUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(EnvType.CLIENT)
public final class ClientSetup {
   private ClientSetup() {
   }

   public static void register() {
      ClientNetwork.register();
      Empty.setClientHooks(
         stack -> AnimationUtils.playCustomUseReequipAnimation(Minecraft.getInstance(), stack),
         CameraShake::trigger,
         () -> {
            Minecraft mc = Minecraft.getInstance();
            return mc.player == null ? ItemStack.EMPTY : mc.player.getMainHandItem();
         },
         () -> AnimationUtils.temp = null,
         AnimationUtils::finishAnimationSession,
         () -> Minecraft.getInstance().player == null ? Items.AIR : Minecraft.getInstance().player.getMainHandItem().getItem(),
         () -> AnimationUtils.getAnimationSessionId(Minecraft.getInstance()),
         () -> {
            AnimationUtils.refreshAnimationControllerId(Minecraft.getInstance());
            return AnimationUtils.getAnimationControllerId(Minecraft.getInstance());
         }
      );
      Empty.setClientSoundHooks(ClientSoundHandler::playLocal, ClientSoundHandler::stopLocalSounds);
      Empty.setClientNetworkHooks(
         (hand, item) -> ClientNetwork.sendCustomUsing(hand, item, 1),
         ClientNetwork::sendFinishUse
      );
      ClientReloadEvents.register();
      FirstPersonModEvents.register();
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new FoodDefinitionManager());
      ClientTickEvents.END_CLIENT_TICK.register(CameraShake::tickClient);
   }
}


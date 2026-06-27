package dev.zenfyr.andromeda.common.client;

import com.mojang.blaze3d.vertex.*;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.config.handler.MultiConfigHandler;
import dev.zenfyr.pulsar.api.client.creativetab.CreativeModeTabAnimation;
import dev.zenfyr.pulsar.api.platform.Platform;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class AndromedaClient implements ClientModInitializer {

  private static AndromedaClient instance;

  public static final MultiConfigHandler CLIENT = new MultiConfigHandler(
      ModuleManager.get(),
      Platform.getPlatform().getConfigDir(),
      "client",
      RegisterConfigEvent.CLIENT);
  private static final Identifier BACKGROUND_TEXTURE = Andromeda.id("textures/gui/background.png");
  private static final Identifier GALAXY_TEXTURE = Andromeda.id("textures/gui/galaxy.png");

  @Override
  public void onInitializeClient() {
    var manager = ModuleManager.get();
    Andromeda.get().onMergedEntryPoint(manager);
    instance = this;

    CLIENT.loadAll();
    CLIENT.saveAll();

    InitEvents.CLIENT.invoker().onModuleClientInit().runEntrypoint();

    if (Andromeda.GROUP.isPresent()) {
      CreativeModeTabAnimation.setIconAnimation(
          Andromeda.GROUP.orThrow(), (tab, graphics, x, y, selected, isTopRow) -> {
            var pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(x + 8, y + 8);
            graphics.blit(
                RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, -8, -8, 0f, 0f, 16, 16, 16, 16);
            pose.rotate(Util.getMillis() * 0.0005f);
            graphics.blit(
                RenderPipelines.GUI_TEXTURED, GALAXY_TEXTURE, -8, -8, 0f, 0f, 16, 16, 16, 16);
            pose.popMatrix();
          });
    }
  }

  public static AndromedaClient get() {
    return instance;
  }
}

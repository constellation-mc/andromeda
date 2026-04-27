package dev.zenfyr.andromeda.common.client;

import com.mojang.blaze3d.vertex.*;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.event.InitEvents;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.config.handler.MultiConfigHandler;
import dev.zenfyr.pulsar.creativetab.CreativeModeTabAnimaton;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

public class AndromedaClient implements ClientModInitializer {

  private static AndromedaClient instance;

  public static final MultiConfigHandler CLIENT = new MultiConfigHandler(
      ModuleManager.get(),
      FabricLoader.getInstance().getConfigDir(),
      "client",
      RegisterConfigEvent.CLIENT);
  private static final ResourceLocation BACKGROUND_TEXTURE =
      Andromeda.id("textures/gui/background.png");
  private static final ResourceLocation GALAXY_TEXTURE = Andromeda.id("textures/gui/galaxy.png");

  @Override
  public void onInitializeClient() {
    Andromeda.get().onMergedEntryPoint();
    instance = this;

    var manager = ModuleManager.get();

    CLIENT.loadAll();
    CLIENT.saveAll();

    InitEvents.CLIENT.invoker().onModuleClientInit().runEntrypoint();

    if (Andromeda.GROUP.isPresent()) {
      CreativeModeTabAnimaton.setIconAnimation(
          Andromeda.GROUP.orThrow(), (tab, graphics, x, y, selected, isTopRow) -> {
            // TODO: rotate the texture on centered axis
            var pose = graphics.pose();
            pose.pushMatrix();
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, x, y, 0, 1);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, GALAXY_TEXTURE, x, y, 0, 1);
          });
    }
  }

  public static AndromedaClient get() {
    return instance;
  }
}

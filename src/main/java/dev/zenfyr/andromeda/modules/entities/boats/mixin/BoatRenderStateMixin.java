package dev.zenfyr.andromeda.modules.entities.boats.mixin;

import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import dev.zenfyr.andromeda.modules.entities.boats.client.RenderStateDuck;
import net.fabricmc.api.EnvType;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import org.spongepowered.asm.mixin.Mixin;

@MixinEnvironment(EnvType.CLIENT)
@Mixin(BoatRenderState.class)
public class BoatRenderStateMixin implements RenderStateDuck {
  private BlockModelRenderState andromeda$blockRenderState = new BlockModelRenderState();

  @Override
  public BlockModelRenderState andromeda$blockRenderState() {
    return this.andromeda$blockRenderState;
  }
}

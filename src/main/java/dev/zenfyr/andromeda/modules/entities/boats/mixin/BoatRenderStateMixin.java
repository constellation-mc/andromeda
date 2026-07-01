package dev.zenfyr.andromeda.modules.entities.boats.mixin;

import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import dev.zenfyr.andromeda.modules.entities.boats.client.RenderStateDuck;
import dev.zenfyr.pulsar.api.platform.CEnvType;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import org.spongepowered.asm.mixin.Mixin;

@MixinEnvironment(CEnvType.CLIENT)
@Mixin(BoatRenderState.class)
public class BoatRenderStateMixin implements RenderStateDuck {
  private BlockModelRenderState andromeda$blockRenderState = new BlockModelRenderState();

  @Override
  public BlockModelRenderState andromeda$blockRenderState() {
    return this.andromeda$blockRenderState;
  }
}

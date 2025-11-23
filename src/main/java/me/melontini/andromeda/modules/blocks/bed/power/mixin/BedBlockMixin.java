package me.melontini.andromeda.modules.blocks.bed.power.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.andromeda.modules.blocks.bed.power.Power;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BedBlock.class)
abstract class BedBlockMixin extends Block {

  public BedBlockMixin(Properties settings) {
    super(settings);
  }

  @ModifyExpressionValue(at = @At(value = "CONSTANT", args = "floatValue=5.0F"), method = "use")
  public float andromeda$explosionRedirect(
      float power,
      @Local(argsOnly = true) Level world,
      @Local(argsOnly = true) BlockPos pos,
      @Local(argsOnly = true) BlockState state,
      @Local(argsOnly = true) Player player,
      @Local(argsOnly = true) InteractionHand hand) {
    if (world.isClientSide()) return power;

    var supplier = LootContextBuilder.block(
        world, builder -> builder.origin(pos).state(state).tool(player, hand).thisEntity(player));
    var config = world.am$get(Power.CONFIG);
    return config.available.asBoolean(supplier) ? config.power.asFloat(supplier) : power;
  }
}

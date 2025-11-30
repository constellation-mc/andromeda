package dev.zenfyr.andromeda.modules.misc.unknown;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.pulsar.client.particles.ScreenParticleHelper;
import dev.zenfyr.pulsar.util.TextUtil;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

public class RoseOfTheValley extends BlockItem {

  public static final Keeper<FlowerBlock> ROSE_OF_THE_VALLEY_BLOCK = Keeper.create();
  public static final Keeper<RoseOfTheValley> ROSE_OF_THE_VALLEY = Keeper.create();

  public RoseOfTheValley(Block block, Properties settings) {
    super(block, settings);
  }

  static void init() {
    RoseOfTheValley.ROSE_OF_THE_VALLEY_BLOCK.init(Registry.register(
        BuiltInRegistries.BLOCK,
        id("rose_of_the_valley"),
        new FlowerBlock(
            MobEffects.REGENERATION,
            12,
            BlockBehaviour.Properties.copy(Blocks.LILY_OF_THE_VALLEY))));
    RoseOfTheValley.ROSE_OF_THE_VALLEY.init(Registry.register(
        BuiltInRegistries.ITEM,
        id("rose_of_the_valley"),
        new RoseOfTheValley(
            RoseOfTheValley.ROSE_OF_THE_VALLEY_BLOCK.orThrow(),
            new FabricItemSettings().rarity(Rarity.UNCOMMON))));
  }

  @Environment(EnvType.CLIENT)
  static void onClient() {
    BlockRenderLayerMap.INSTANCE.putBlocks(
        RenderType.cutout(), RoseOfTheValley.ROSE_OF_THE_VALLEY_BLOCK.orThrow());
  }

  @Override
  public void appendHoverText(
      ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
    tooltip.add(TextUtil.translatable("tooltip.andromeda.rose_of_the_valley")
        .withStyle(ChatFormatting.GRAY));
  }

  public static void handleClick(ItemStack stack, ItemStack otherStack, Player player) {
    player.getInventory().placeItemBackInInventory(new ItemStack(ROSE_OF_THE_VALLEY.orThrow()));
    stack.shrink(1);
    otherStack.shrink(1);
    if (player.level.isClientSide) {
      var client = Minecraft.getInstance();
      int x = (int) (client.mouseHandler.xpos()
          * (double) client.getWindow().getGuiScaledWidth()
          / (double) client.getWindow().getScreenWidth());
      int y = (int) (client.mouseHandler.ypos()
          * (double) client.getWindow().getGuiScaledHeight()
          / (double) client.getWindow().getScreenHeight());
      ScreenParticleHelper.addParticles(ParticleTypes.END_ROD, x, y, 0.5, 0.5, 0.08, 10);
    }
  }
}

package dev.zenfyr.andromeda.modules.misc.unknown;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.pulsar.client.particles.ScreenParticleHelper;
import dev.zenfyr.pulsar.util.TextUtil;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RoseOfTheValley extends BlockItem {

  public static final Keeper<FlowerBlock> ROSE_OF_THE_VALLEY_BLOCK = Keeper.create();
  public static final Keeper<RoseOfTheValley> ROSE_OF_THE_VALLEY = Keeper.create();

  public RoseOfTheValley(Block block, Properties settings) {
    super(block, settings);
  }

  static void init() {
    var blockKey = Andromeda.key(Registries.BLOCK, "rose_of_the_valley");
    RoseOfTheValley.ROSE_OF_THE_VALLEY_BLOCK.init(Registry.register(
        BuiltInRegistries.BLOCK,
        blockKey,
        new FlowerBlock(
            MobEffects.REGENERATION,
            12,
            BlockBehaviour.Properties.ofFullCopy(Blocks.LILY_OF_THE_VALLEY).setId(blockKey))));

    var itemKey = Andromeda.key(Registries.ITEM, "rose_of_the_valley");
    RoseOfTheValley.ROSE_OF_THE_VALLEY.init(Registry.register(
        BuiltInRegistries.ITEM,
        itemKey,
        new RoseOfTheValley(
            RoseOfTheValley.ROSE_OF_THE_VALLEY_BLOCK.orThrow(),
            new Item.Properties().setId(itemKey).rarity(Rarity.UNCOMMON))));
  }

  @Environment(EnvType.CLIENT)
  static void onClient() {
    BlockRenderLayerMap.putBlocks(
        ChunkSectionLayer.CUTOUT, RoseOfTheValley.ROSE_OF_THE_VALLEY_BLOCK.orThrow());
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      TooltipDisplay tooltipDisplay,
      Consumer<Component> consumer,
      TooltipFlag tooltipFlag) {
    consumer.accept(TextUtil.translatable("tooltip.andromeda.rose_of_the_valley")
        .withStyle(ChatFormatting.GRAY));
  }

  public static void handleClick(ItemStack stack, ItemStack otherStack, Player player) {
    player.getInventory().placeItemBackInInventory(new ItemStack(ROSE_OF_THE_VALLEY.orThrow()));
    stack.shrink(1);
    otherStack.shrink(1);
    if (player.level.isClientSide()) {
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

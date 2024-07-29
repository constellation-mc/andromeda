package me.melontini.andromeda.modules.misc.unknown;

import static me.melontini.andromeda.common.Andromeda.id;

import java.util.List;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RoseOfTheValley extends BlockItem {

  public static final Keeper<FlowerBlock> ROSE_OF_THE_VALLEY_BLOCK = Keeper.create();
  public static final Keeper<RoseOfTheValley> ROSE_OF_THE_VALLEY = Keeper.create();

  public RoseOfTheValley(Block block, Settings settings) {
    super(block, settings);
  }

  static void init() {
    RoseOfTheValley.ROSE_OF_THE_VALLEY_BLOCK.init(RegistryUtil.register(
        Registries.BLOCK,
        id("rose_of_the_valley"),
        () -> new FlowerBlock(
            StatusEffects.REGENERATION,
            12,
            AbstractBlock.Settings.copy(Blocks.LILY_OF_THE_VALLEY))));
    RoseOfTheValley.ROSE_OF_THE_VALLEY.init(RegistryUtil.register(
        Registries.ITEM,
        id("rose_of_the_valley"),
        () -> new RoseOfTheValley(
            RoseOfTheValley.ROSE_OF_THE_VALLEY_BLOCK.orThrow(),
            new FabricItemSettings().rarity(Rarity.UNCOMMON))));
  }

  @Environment(EnvType.CLIENT)
  static void onClient() {
    ROSE_OF_THE_VALLEY_BLOCK.ifPresent(
        b -> BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), b));
  }

  @Override
  public void appendTooltip(
      ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
    tooltip.add(
        TextUtil.translatable("tooltip.andromeda.rose_of_the_valley").formatted(Formatting.GRAY));
  }

  public static void handleClick(ItemStack stack, ItemStack otherStack, PlayerEntity player) {
    player.getInventory().offerOrDrop(new ItemStack(ROSE_OF_THE_VALLEY.orThrow()));
    stack.decrement(1);
    otherStack.decrement(1);
    if (player.world.isClient) {
      var client = MinecraftClient.getInstance();
      int x = (int) (client.mouse.getX()
          * (double) client.getWindow().getScaledWidth()
          / (double) client.getWindow().getWidth());
      int y = (int) (client.mouse.getY()
          * (double) client.getWindow().getScaledHeight()
          / (double) client.getWindow().getHeight());
      ScreenParticleHelper.addParticles(ParticleTypes.END_ROD, x, y, 0.5, 0.5, 0.08, 10);
    }
  }
}

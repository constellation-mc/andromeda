package me.melontini.andromeda.items;

import me.melontini.andromeda.registries.ItemRegistry;
import me.melontini.andromeda.util.AndromedaTexts;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RoseOfTheValley extends BlockItem {

    public RoseOfTheValley(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(AndromedaTexts.ROSE_OF_THE_VALLEY_TOOLTIP);
    }

    public static void handleClick(ItemStack stack, ItemStack otherStack, PlayerEntity player) {
        player.getInventory().offerOrDrop(new ItemStack(ItemRegistry.ROSE_OF_THE_VALLEY));
        stack.decrement(1);
        otherStack.decrement(1);
        if (player.world.isClient) {
            var client = MinecraftClient.getInstance();
            int x = (int) (client.mouse.getX() * (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth());
            int y = (int) (client.mouse.getY() * (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight());
            ScreenParticleHelper.addParticles(ParticleTypes.END_ROD, x, y, 0.5, 0.5, 0.08, 10);
        }
    }
}

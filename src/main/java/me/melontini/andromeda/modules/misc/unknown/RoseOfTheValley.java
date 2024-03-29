package me.melontini.andromeda.modules.misc.unknown;

import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RoseOfTheValley extends BlockItem {

    public RoseOfTheValley(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(TextUtil.translatable("tooltip.andromeda.rose_of_the_valley").formatted(Formatting.GRAY));
    }

    public static void handleClick(ItemStack stack, ItemStack otherStack, PlayerEntity player) {
        player.getInventory().offerOrDrop(new ItemStack(Main.ROSE_OF_THE_VALLEY.orThrow()));
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

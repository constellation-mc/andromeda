package me.melontini.andromeda.items.minecarts;

import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.vehicle.SpawnerMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpawnerMinecartItem extends AndromedaMinecartItem<SpawnerMinecartEntity> {

    public SpawnerMinecartItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) if (nbt.getString("Entity") != null) {
            tooltip.add(TextUtil.translatable("tooltip.andromeda.spawner_minecart.filled", Registries.ENTITY_TYPE.get(Identifier.tryParse(nbt.getString("Entity"))).getName()).formatted(Formatting.GRAY));
        }
    }

    @Override
    protected void onCreate(ItemStack stack, SpawnerMinecartEntity entity) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) if (nbt.getString("Entity") != null) {
            entity.getLogic().setEntityId(Registries.ENTITY_TYPE.get(Identifier.tryParse(nbt.getString("Entity"))), entity.world, entity.world.random, entity.getBlockPos());
        }
    }

    @Override
    protected SpawnerMinecartEntity createEntity(World world, double x, double y, double z) {
        return new SpawnerMinecartEntity(world, x, y, z);
    }
}

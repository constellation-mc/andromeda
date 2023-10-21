package me.melontini.andromeda.items.minecarts;

import me.melontini.andromeda.entity.vehicle.minecarts.JukeboxMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class JukeBoxMinecartItem extends AndromedaMinecartItem<JukeboxMinecartEntity> {

    public JukeBoxMinecartItem(Settings settings) {
        super(settings);
    }

    @Override
    protected void onCreate(ItemStack stack, JukeboxMinecartEntity entity) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) if (nbt.getCompound("Items") != null) {
            entity.record = ItemStack.fromNbt(nbt.getCompound("Items"));
        }
        if (nbt != null) if (nbt.getCompound("Items") != null) entity.startPlaying();
    }

    @Override
    protected JukeboxMinecartEntity createEntity(World world, double x, double y, double z) {
        return new JukeboxMinecartEntity(world, x, y, z);
    }
}

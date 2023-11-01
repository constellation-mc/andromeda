package me.melontini.andromeda.items.minecarts;

import me.melontini.andromeda.entity.vehicle.minecarts.AnvilMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AnvilMinecartItem extends AndromedaMinecartItem<AnvilMinecartEntity> {

    //TODO minecart block picking
    public AnvilMinecartItem(Settings settings) {
        super(settings);
    }

    @Override
    protected void onCreate(ItemStack stack, AnvilMinecartEntity entity) {
    }

    @Override
    protected AnvilMinecartEntity createEntity(World world, double x, double y, double z) {
        return new AnvilMinecartEntity(world, x, y, z);
    }
}

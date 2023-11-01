package me.melontini.andromeda.items.boats;

import me.melontini.andromeda.entity.vehicle.boats.FurnaceBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;

public class FurnaceBoatItem extends AndromedaBoatItem<FurnaceBoatEntity> {

    public FurnaceBoatItem(BoatEntity.Type type, Settings settings) {
        super(type, settings);
    }

    @Override
    protected FurnaceBoatEntity createEntity(World world, double x, double y, double z) {
        return new FurnaceBoatEntity(world, x, y, z);
    }
}

package me.melontini.andromeda.modules.entities.boats.items;

import me.melontini.andromeda.modules.entities.boats.entities.HopperBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;

public class HopperBoatItem extends AndromedaBoatItem<HopperBoatEntity> {

    public HopperBoatItem(BoatEntity.Type type, Settings settings) {
        super(type, settings);
    }

    @Override
    protected HopperBoatEntity createEntity(World world, double x, double y, double z) {
        return new HopperBoatEntity(world, x, y, z);
    }
}

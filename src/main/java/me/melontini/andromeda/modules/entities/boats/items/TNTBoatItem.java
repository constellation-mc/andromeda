package me.melontini.andromeda.modules.entities.boats.items;

import me.melontini.andromeda.modules.entities.boats.entities.TNTBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;

public class TNTBoatItem extends AndromedaBoatItem<TNTBoatEntity> {

    public TNTBoatItem(BoatEntity.Type type, Settings settings) {
        super(type, settings);
    }

    @Override
    protected TNTBoatEntity createEntity(World world, double x, double y, double z) {
        return new TNTBoatEntity(world, x, y, z);
    }
}

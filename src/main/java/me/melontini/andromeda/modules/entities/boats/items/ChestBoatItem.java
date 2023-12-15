package me.melontini.andromeda.modules.entities.boats.items;


import me.melontini.andromeda.modules.entities.boats.entities.ChestBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;

public class ChestBoatItem extends AndromedaBoatItem<ChestBoatEntity> {

    public ChestBoatItem(BoatEntity.Type type, Settings settings) {
        super(type, settings);
    }

    @Override
    protected ChestBoatEntity createEntity(World world, double x, double y, double z) {
        return new ChestBoatEntity(world, x, y, z);
    }
}
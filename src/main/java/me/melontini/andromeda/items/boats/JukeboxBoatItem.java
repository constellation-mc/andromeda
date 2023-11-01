package me.melontini.andromeda.items.boats;

import me.melontini.andromeda.entity.vehicle.boats.JukeboxBoatEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.world.World;

public class JukeboxBoatItem extends AndromedaBoatItem<JukeboxBoatEntity> {

    public JukeboxBoatItem(BoatEntity.Type type, Settings settings) {
        super(type, settings);
    }

    @Override
    protected JukeboxBoatEntity createEntity(World world, double x, double y, double z) {
        return new JukeboxBoatEntity(world, x, y, z);
    }
}

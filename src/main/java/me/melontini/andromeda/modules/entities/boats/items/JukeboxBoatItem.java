package me.melontini.andromeda.modules.entities.boats.items;

import me.melontini.andromeda.modules.entities.boats.entities.JukeboxBoatEntity;
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

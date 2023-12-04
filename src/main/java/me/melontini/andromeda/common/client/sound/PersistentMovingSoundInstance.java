package me.melontini.andromeda.common.client.sound;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

import java.util.UUID;

public class PersistentMovingSoundInstance extends MovingSoundInstance {

    private final ClientWorld world;
    private final UUID entityId;

    public PersistentMovingSoundInstance(SoundEvent soundEvent, SoundCategory soundCategory, UUID entityId, ClientWorld world) {
        super(soundEvent, soundCategory);
        this.volume = 3;
        this.pitch = 1;
        this.world = world;
        this.entityId = entityId;
    }

    @Override
    public void tick() {
        Entity entity = world.getEntityLookup().get(entityId);
        if (entity != null) {
            this.volume = 3;
            this.x = entity.getX();
            this.y = entity.getY();
            this.z = entity.getZ();
        } else {
            //this sucks
            this.volume = 0;
        }
    }
}

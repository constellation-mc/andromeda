package me.melontini.andromeda.modules.items.infinite_totem.client;

import me.melontini.andromeda.modules.items.infinite_totem.Content;
import me.melontini.andromeda.util.AndromedaPackets;
import me.melontini.dark_matter.api.base.util.MakeSure;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.registry.Registry;

import java.util.UUID;

public class Client {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(AndromedaPackets.USED_CUSTOM_TOTEM, (client, handler, buf, responseSender) -> {
            UUID id = buf.readUuid();
            ItemStack stack = buf.readItemStack();
            DefaultParticleType particle = (DefaultParticleType)Registry.PARTICLE_TYPE.get(buf.readIdentifier());
            client.execute(() -> {
                Entity entity = MakeSure.notNull(client.world, "client.world").getEntityLookup().get(id);
                client.particleManager.addEmitter(MakeSure.notNull(entity, "(Andromeda) Client received invalid entity ID"), particle, 30);
                client.world.playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_TOTEM_USE, entity.getSoundCategory(), 1.0F, 1.0F, false);
                if (entity == client.player) client.gameRenderer.showFloatingItem(stack);
            });
        });

        Content.KNOCKOFF_TOTEM_PARTICLE.ifPresent(t -> ParticleFactoryRegistry.getInstance().register(t, KnockoffTotemParticle.Factory::new));
    }
}

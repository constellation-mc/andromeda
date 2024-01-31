package me.melontini.andromeda.modules.entities.minecarts.items;

import me.melontini.andromeda.modules.entities.minecarts.MinecartEntities;
import me.melontini.andromeda.modules.entities.minecarts.entities.JukeboxMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class JukeboxMinecartItem extends AndromedaMinecartItem<JukeboxMinecartEntity> {

    public JukeboxMinecartItem(Settings settings) {
        super(MinecartEntities.JUKEBOX_MINECART_ENTITY, settings);
    }

    @Override
    protected void onCreate(ItemStack stack, JukeboxMinecartEntity entity) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) if (nbt.getCompound("Items") != null) {
            entity.record = ItemStack.fromNbt(nbt.getCompound("Items"));
            if (!entity.record.isEmpty()) entity.startPlaying();
        }
    }
}

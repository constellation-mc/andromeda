package me.melontini.andromeda.modules.entities.minecarts.items;

import me.melontini.andromeda.modules.entities.minecarts.MinecartEntities;
import me.melontini.andromeda.modules.entities.minecarts.entities.NoteBlockMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class NoteBlockMinecartItem extends AndromedaMinecartItem<NoteBlockMinecartEntity> {

    public NoteBlockMinecartItem(Settings settings) {
        super(MinecartEntities.NOTEBLOCK_MINECART_ENTITY, settings);
    }

    @Override
    protected void onCreate(ItemStack stack, NoteBlockMinecartEntity entity) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) if (nbt.getInt("Note") >= 0) {
            entity.note = nbt.getInt("Note");
        }
    }
}

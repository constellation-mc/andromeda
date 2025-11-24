package me.melontini.andromeda.modules.entities.minecarts.items;

import me.melontini.andromeda.modules.entities.minecarts.MinecartEntities;
import me.melontini.andromeda.modules.entities.minecarts.entities.NoteBlockMinecartEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class NoteBlockMinecartItem extends AndromedaMinecartItem<NoteBlockMinecartEntity> {

  public NoteBlockMinecartItem(Properties settings) {
    super(MinecartEntities.NOTEBLOCK_MINECART_ENTITY, settings);
  }

  @Override
  protected void onCreate(ItemStack stack, NoteBlockMinecartEntity entity) {
    CompoundTag nbt = stack.getTag();
    if (nbt != null)
      if (nbt.getInt("Note") >= 0) {
        entity.note = nbt.getInt("Note");
      }
  }
}

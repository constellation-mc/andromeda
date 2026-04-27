package dev.zenfyr.andromeda.modules.entities.minecarts.items;

import dev.zenfyr.andromeda.modules.entities.minecarts.MinecartEntities;
import dev.zenfyr.andromeda.modules.entities.minecarts.entities.NoteBlockMinecartEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public class NoteBlockMinecartItem extends AndromedaMinecartItem<NoteBlockMinecartEntity> {

  public NoteBlockMinecartItem(Properties settings) {
    super(MinecartEntities.NOTEBLOCK_MINECART_ENTITY, settings);
  }

  @Override
  protected void onCreate(ItemStack stack, NoteBlockMinecartEntity entity) {
    var data = stack.get(DataComponents.ENTITY_DATA);
    if (data != null) {
      var nbt = data.copyTagWithoutId();
      if (nbt.getIntOr("Note", 0) >= 0) {
        entity.note = nbt.getIntOr("Note", 0);
      }
    }
  }
}

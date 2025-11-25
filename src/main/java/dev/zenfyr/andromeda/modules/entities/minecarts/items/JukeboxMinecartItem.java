package dev.zenfyr.andromeda.modules.entities.minecarts.items;

import dev.zenfyr.andromeda.modules.entities.minecarts.MinecartEntities;
import dev.zenfyr.andromeda.modules.entities.minecarts.entities.JukeboxMinecartEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class JukeboxMinecartItem extends AndromedaMinecartItem<JukeboxMinecartEntity> {

  public JukeboxMinecartItem(Properties settings) {
    super(MinecartEntities.JUKEBOX_MINECART_ENTITY, settings);
  }

  @Override
  protected void onCreate(ItemStack stack, JukeboxMinecartEntity entity) {
    CompoundTag nbt = stack.getTag();
    if (nbt != null)
      if (nbt.getCompound("Items") != null) {
        entity.record = ItemStack.of(nbt.getCompound("Items"));
        if (!entity.record.isEmpty()) entity.startPlaying();
      }
  }
}

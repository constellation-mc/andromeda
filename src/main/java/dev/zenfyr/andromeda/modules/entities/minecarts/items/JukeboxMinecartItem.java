package dev.zenfyr.andromeda.modules.entities.minecarts.items;

import dev.zenfyr.andromeda.modules.entities.minecarts.MinecartEntities;
import dev.zenfyr.andromeda.modules.entities.minecarts.entities.JukeboxMinecartEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

public class JukeboxMinecartItem extends AndromedaMinecartItem<JukeboxMinecartEntity> {

  public JukeboxMinecartItem(Properties settings) {
    super(MinecartEntities.JUKEBOX_MINECART_ENTITY, settings);
  }

  @Override
  protected void onCreate(ItemStack stack, JukeboxMinecartEntity entity) {
    var data = stack.get(DataComponents.ENTITY_DATA);
    if (data != null) {
      var nbt = data.copyTagWithoutId();
      if (nbt.getCompound("Items").isPresent()) {
        entity.record = ItemStack.OPTIONAL_CODEC
            .decode(NbtOps.INSTANCE, nbt.getCompoundOrEmpty("Items"))
            .result()
            .orElseThrow()
            .getFirst();
        if (!entity.record.isEmpty()) entity.startPlaying();
      }
    }
  }
}

package me.melontini.andromeda.modules.entities.minecarts.items;

import me.melontini.andromeda.modules.entities.minecarts.MinecartEntities;
import me.melontini.andromeda.modules.entities.minecarts.entities.JukeboxMinecartEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class JukeboxMinecartItem extends AndromedaMinecartItem<JukeboxMinecartEntity> {

  public JukeboxMinecartItem(Settings settings) {
    super(MinecartEntities.JUKEBOX_MINECART_ENTITY, settings);
  }

  @Override
  protected void onCreate(ItemStack stack, JukeboxMinecartEntity entity) {
    NbtCompound nbt =
        stack.getOrDefault(DataComponentTypes.ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
    if (nbt.getCompound("Items") != null) {
      ItemStack.fromNbt(entity.getRegistryManager(), nbt.getCompound("Items")).ifPresent(stack1 -> {
        entity.record = stack1;
        entity.startPlaying();
      });
    }
  }
}

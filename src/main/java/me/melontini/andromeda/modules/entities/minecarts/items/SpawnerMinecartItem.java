package me.melontini.andromeda.modules.entities.minecarts.items;

import java.util.List;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SpawnerMinecartItem extends AndromedaMinecartItem<MinecartSpawner> {

  public SpawnerMinecartItem(Properties settings) {
    super(Keeper.now(EntityType.SPAWNER_MINECART), settings);
  }

  @Override
  public void appendHoverText(
          ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
    CompoundTag nbt = stack.getTag();
    if (nbt != null)
      if (nbt.getString("Entity") != null) {
        tooltip.add(TextUtil.translatable(
                "tooltip.andromeda.spawner_minecart.filled",
                BuiltInRegistries.ENTITY_TYPE
                    .get(new ResourceLocation(nbt.getString("Entity")))
                    .getDescription())
            .withStyle(ChatFormatting.GRAY));
      }
  }

  @Override
  protected void onCreate(ItemStack stack, MinecartSpawner entity) {
    CompoundTag nbt = stack.getTag();
    if (nbt != null)
      if (nbt.getString("Entity") != null) {
        entity
            .getSpawner()
            .setEntityId(
                BuiltInRegistries.ENTITY_TYPE.get(new ResourceLocation(nbt.getString("Entity"))),
                entity.level,
                entity.level.random,
                entity.blockPosition());
      }
  }
}

package dev.zenfyr.andromeda.modules.entities.minecarts.items;

import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.pulsar.api.util.TextUtil;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class SpawnerMinecartItem extends AndromedaMinecartItem<MinecartSpawner> {

  public SpawnerMinecartItem(Properties settings) {
    super(Keeper.now(EntityType.SPAWNER_MINECART), settings);
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      TooltipDisplay display,
      Consumer<Component> consumer,
      TooltipFlag tooltipFlag) {
    var data = stack.get(DataComponents.ENTITY_DATA);
    if (data != null) {
      var nbt = data.copyTagWithoutId();
      if (nbt.getString("Entity").isPresent()) {
        consumer.accept(TextUtil.translatable(
                "tooltip.andromeda.spawner_minecart.filled",
                BuiltInRegistries.ENTITY_TYPE
                    .getValue(ResourceLocation.tryParse(nbt.getStringOr("Entity", "minecraft:pig")))
                    .getDescription())
            .withStyle(ChatFormatting.GRAY));
      }
    }
  }

  @Override
  protected void onCreate(ItemStack stack, MinecartSpawner entity) {
    var data = stack.get(DataComponents.ENTITY_DATA);
    if (data != null) {
      var nbt = data.copyTagWithoutId();
      if (nbt.getString("Entity").isPresent()) {
        entity
            .getSpawner()
            .setEntityId(
                BuiltInRegistries.ENTITY_TYPE.getValue(
                    ResourceLocation.tryParse(nbt.getStringOr("Entity", "minecraft:pig"))),
                entity.level,
                entity.level.random,
                entity.blockPosition());
      }
    }
  }
}

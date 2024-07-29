package me.melontini.andromeda.modules.entities.minecarts.items;

import java.util.List;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.SpawnerMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SpawnerMinecartItem extends AndromedaMinecartItem<SpawnerMinecartEntity> {

  public SpawnerMinecartItem(Settings settings) {
    super(Keeper.now(EntityType.SPAWNER_MINECART), settings);
  }

  @Override
  public void appendTooltip(
      ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
    NbtCompound nbt = stack.getNbt();
    if (nbt != null)
      if (nbt.getString("Entity") != null) {
        tooltip.add(TextUtil.translatable(
                "tooltip.andromeda.spawner_minecart.filled",
                Registries.ENTITY_TYPE
                    .get(new Identifier(nbt.getString("Entity")))
                    .getName())
            .formatted(Formatting.GRAY));
      }
  }

  @Override
  protected void onCreate(ItemStack stack, SpawnerMinecartEntity entity) {
    NbtCompound nbt = stack.getNbt();
    if (nbt != null)
      if (nbt.getString("Entity") != null) {
        entity
            .getLogic()
            .setEntityId(
                Registries.ENTITY_TYPE.get(new Identifier(nbt.getString("Entity"))),
                entity.world,
                entity.world.random,
                entity.getBlockPos());
      }
  }
}

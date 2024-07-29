package me.melontini.andromeda.modules.items.pouches.items;

import java.util.List;
import lombok.Getter;
import me.melontini.andromeda.common.util.WorldUtil;
import me.melontini.andromeda.modules.items.pouches.Main;
import me.melontini.andromeda.modules.items.pouches.entities.PouchEntity;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;

@Getter
public class PouchItem extends Item implements ProjectileItem {

  private final PouchEntity.Type type;

  public PouchItem(PouchEntity.Type type, Item.Settings settings) {
    super(settings);
    this.type = type;
  }

  @Override
  public void appendTooltip(
      ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
    if (type.isAdvanced() && Debug.Keys.DISPLAY_TRACKED_VALUES.isPresent()) {
      tooltip.add(
          TextUtil.literal("Loot: " + this.getType().getLootId(stack).getValue()).formatted(Formatting.GRAY));
    }
  }

  @Override
  public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
    ItemStack itemStack = user.getStackInHand(hand);
    world.playSound(
        null,
        user.getX(),
        user.getY(),
        user.getZ(),
        SoundEvents.ENTITY_SNOWBALL_THROW,
        SoundCategory.NEUTRAL,
        0.5F,
        0.4F / (world.random.nextFloat() * 0.4F + 0.8F));
    if (!world.isClient) {
      var entity = new PouchEntity(user, world);
      entity.setPouchType(this.type);
      entity.setPos(user.getX(), user.getEyeY() - 0.1F, user.getZ());
      entity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
      entity.setItem(itemStack);
      world.spawnEntity(entity);
    }

    user.incrementStat(Stats.USED.getOrCreateStat(this));
    if (!user.getAbilities().creativeMode) {
      itemStack.decrement(1);
    }

    return TypedActionResult.success(itemStack, world.isClient());
  }

  @Override
  public ActionResult useOnEntity(
      ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
    if (!user.getWorld().isClient()) {
      var stacks = WorldUtil.prepareLoot(user.getWorld(), type.getLootId(stack));

      boolean success = false;
      if (entity instanceof PlayerEntity player) {
        var storage = PlayerInventoryStorage.of(player);
        stacks.forEach(
            itemStack -> Main.tryInsertItem(user.getWorld(), player.getPos(), itemStack, storage));
        success = true;
      } else if (entity instanceof InventoryOwner io) {
        var storage = InventoryStorage.of(io.getInventory(), null);
        stacks.forEach(itemStack ->
            Main.tryInsertItem(entity.getWorld(), entity.getPos(), itemStack, storage));
        success = true;
      }

      if (success) {
        if (user.getWorld() instanceof ServerWorld sw) {
          sw.spawnParticles(
              new ItemStackParticleEffect(ParticleTypes.ITEM, stack),
              entity.getX(),
              entity.getY(),
              entity.getZ(),
              10,
              0.2,
              0.2,
              0.2,
              0.25);
        }

        if (!user.getAbilities().creativeMode) {
          stack.decrement(1);
        }
        return ActionResult.SUCCESS;
      }
    }
    return ActionResult.PASS;
  }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        var pouch = new PouchEntity(pos.getX(), pos.getY(), pos.getZ(), world);
        pouch.setPouchType(((PouchItem) stack.getItem()).getType());
        return pouch;
    }
}

package dev.zenfyr.andromeda.modules.items.pouches.items;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.util.MiscUtil;
import dev.zenfyr.andromeda.modules.items.pouches.PouchesMain;
import dev.zenfyr.andromeda.modules.items.pouches.entities.PouchEntity;
import dev.zenfyr.pulsar.api.util.TextUtil;
import java.util.function.Consumer;
import lombok.Getter;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

@Getter
public class PouchItem extends Item implements ProjectileItem {

  private final PouchEntity.Type type;

  public PouchItem(PouchEntity.Type type, Properties settings) {
    super(settings);
    this.type = type;
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext context,
      TooltipDisplay display,
      Consumer<Component> consumer,
      TooltipFlag flag) {
    if (flag.isAdvanced() && ModuleManager.get().debug().isVerbose()) {
      consumer.accept(
          TextUtil.literal("Loot: " + this.getType().getLootId(stack).identifier())
              .withStyle(ChatFormatting.GRAY));
    }
  }

  @Override
  public InteractionResult use(Level level, Player user, InteractionHand hand) {
    ItemStack itemStack = user.getItemInHand(hand);
    level.playSound(
        null,
        user.getX(),
        user.getY(),
        user.getZ(),
        SoundEvents.SNOWBALL_THROW,
        SoundSource.NEUTRAL,
        0.5F,
        0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
    if (!level.isClientSide()) {
      var entity = PouchesMain.POUCH.orThrow().create(level, EntitySpawnReason.DISPENSER);
      entity.setPouchType(this.type);
      entity.setPosRaw(user.getX(), user.getEyeY() - 0.1F, user.getZ());
      entity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.5F, 1.0F);
      entity.setItem(itemStack);
      level.addFreshEntity(entity);
    }

    user.awardStat(Stats.ITEM_USED.get(this));
    if (!user.getAbilities().instabuild) {
      itemStack.shrink(1);
    }

    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResult interactLivingEntity(
      ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
    if (!user.level().isClientSide()) {
      var stacks = MiscUtil.prepareLoot(user.level(), type.getLootId(stack));

      boolean success = false;
      if (entity instanceof Player player) {
        var storage = PlayerInventoryStorage.of(player);
        stacks.forEach(itemStack ->
            PouchesMain.tryInsertItem(user.level(), player.position(), itemStack, storage));
        success = true;
      } else if (entity instanceof InventoryCarrier io) {
        var storage = ContainerStorage.of(io.getInventory(), null);
        stacks.forEach(itemStack ->
            PouchesMain.tryInsertItem(entity.level(), entity.position(), itemStack, storage));
        success = true;
      }

      if (success) {
        if (user.level() instanceof ServerLevel sw && !stack.isEmpty()) {
          sw.sendParticles(
              new ItemParticleOption(
                  ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack)),
              entity.getX(),
              entity.getY(),
              entity.getZ(),
              10,
              0.2,
              0.2,
              0.2,
              0.25);
        }

        if (!user.getAbilities().instabuild) {
          stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
      }
    }
    return InteractionResult.PASS;
  }

  @Override
  public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
    var pouch = PouchesMain.POUCH.orThrow().create(level, EntitySpawnReason.DISPENSER);
    pouch.setPos(pos.x(), pos.y(), pos.z());
    pouch.setPouchType(((PouchItem) stack.getItem()).getType());
    return pouch;
  }
}

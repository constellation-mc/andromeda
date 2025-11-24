package me.melontini.andromeda.modules.items.pouches.items;

import java.util.List;
import lombok.Getter;
import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.andromeda.modules.items.pouches.Main;
import me.melontini.andromeda.modules.items.pouches.entities.PouchEntity;
import me.melontini.andromeda.util.Util;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

@Getter
public class PouchItem extends Item {

  private final PouchEntity.Type type;

  public PouchItem(PouchEntity.Type type, Properties settings) {
    super(settings);
    this.type = type;
  }

  @Override
  public void appendHoverText(
      ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
    if (context.isAdvanced() && Util.isDev()) {
      tooltip.add(TextUtil.literal("Loot: " + this.getType().getLootId(stack))
          .withStyle(ChatFormatting.GRAY));
    }
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
    ItemStack itemStack = user.getItemInHand(hand);
    world.playSound(
        null,
        user.getX(),
        user.getY(),
        user.getZ(),
        SoundEvents.SNOWBALL_THROW,
        SoundSource.NEUTRAL,
        0.5F,
        0.4F / (world.random.nextFloat() * 0.4F + 0.8F));
    if (!world.isClientSide) {
      var entity = new PouchEntity(user, world);
      entity.setPouchType(this.type);
      entity.setPosRaw(user.getX(), user.getEyeY() - 0.1F, user.getZ());
      entity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.5F, 1.0F);
      entity.setItem(itemStack);
      world.addFreshEntity(entity);
    }

    user.awardStat(Stats.ITEM_USED.get(this));
    if (!user.getAbilities().instabuild) {
      itemStack.shrink(1);
    }

    return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
  }

  @Override
  public InteractionResult interactLivingEntity(
      ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
    if (!user.level().isClientSide()) {
      var stacks = LootContextBuilder.prepareLoot(user.level(), type.getLootId(stack));

      boolean success = false;
      if (entity instanceof Player player) {
        var storage = PlayerInventoryStorage.of(player);
        stacks.forEach(
            itemStack -> Main.tryInsertItem(user.level(), player.position(), itemStack, storage));
        success = true;
      } else if (entity instanceof InventoryCarrier io) {
        var storage = InventoryStorage.of(io.getInventory(), null);
        stacks.forEach(
            itemStack -> Main.tryInsertItem(entity.level(), entity.position(), itemStack, storage));
        success = true;
      }

      if (success) {
        if (user.level() instanceof ServerLevel sw) {
          sw.sendParticles(
              new ItemParticleOption(ParticleTypes.ITEM, stack),
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
}

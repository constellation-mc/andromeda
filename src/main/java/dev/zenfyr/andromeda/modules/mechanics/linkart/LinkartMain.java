package dev.zenfyr.andromeda.modules.mechanics.linkart;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import dev.zenfyr.andromeda.common.util.Keeper;
import java.util.Set;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Unique;

public class LinkartMain {

  // Used to smooth out acceleration
  @Unique private static final double SAFE_SPEEDUP_THRESHOLD = 0.4;

  @Unique private static final double SMOOTH_SPEEDUP_AMOUNT = 0.2;

  @Unique private static final double SAFE_SPEEDUP_DIFFERENCE = 0.02;

  public static final TagKey<Item> LINKERS =
      TagKey.create(BuiltInRegistries.ITEM.key(), id("linkers"));
  public static final Keeper<AttachmentType<LoadingCarts>> ATTACHMENT = Keeper.create();

  public static void init() {
    ATTACHMENT.init(AttachmentRegistry.create(
        id("loading_carts"),
        builder ->
            builder.initializer(() -> new LoadingCarts(Set.of())).persistent(LoadingCarts.CODEC)));

    ServerTickEvents.START_WORLD_TICK.register(level -> {
      var cfg = level.am$get(Linkart.CONFIG);
      if (cfg.chunkloading) {
        level.getAttachedOrCreate(ATTACHMENT.get()).tick(level);
      }
    });
  }

  public static double limitMovementLength(LinkableMinecart cart, double targetMovementLength) {
    double cartLastMovementLength = cart.linkart$lastMovementLength();

    boolean isLeading = (cart.linkart$getFollowing() == null && cart.linkart$getFollower() != null);
    // Don't limit if we are not the leading minecart
    if (!isLeading) return targetMovementLength;
    // Don't limit if we are below the safe speedup threshold
    if (targetMovementLength <= SAFE_SPEEDUP_THRESHOLD) return targetMovementLength;

    AbstractMinecart follower = cart.linkart$getFollower();
    // Check if there are follower minecarts not at our speed
    while (follower != null) {
      double followerLastMovementLength =
          ((LinkableMinecart) follower).linkart$lastMovementLength();
      if (Math.abs(followerLastMovementLength - cartLastMovementLength) > SAFE_SPEEDUP_DIFFERENCE)
        // If so, maintain same speed
        return cartLastMovementLength;
      follower = ((LinkableMinecart) follower).linkart$getFollower();
    }

    // Otherwise increase our speed slowly
    return Math.min(
        Math.max(cartLastMovementLength + SMOOTH_SPEEDUP_AMOUNT, SAFE_SPEEDUP_THRESHOLD), // min
        targetMovementLength); // max
  }

  public static boolean shouldCollide(Entity source, Entity target) {
    if (source instanceof AbstractMinecart check) {
      int i = 0;

      do {
        if (check == target) {
          return false;
        }

        check = ((LinkableMinecart) check).linkart$getFollower();
        ++i;
      } while (check != null && i < 8);

      check = (AbstractMinecart) source;
      i = 0;

      while (check != target) {
        check = ((LinkableMinecart) check).linkart$getFollowing();
        ++i;
        if (check == null || i >= 8) {
          return true;
        }
      }

      return false;
    }
    return true;
  }

  public static void spawnChainParticles(AbstractMinecart entity) {
    if (!entity.level().isClientSide()) {
      ((ServerLevel) entity.level())
          .sendParticles(
              new ItemParticleOption(
                  ParticleTypes.ITEM, ((LinkableMinecart) entity).linkart$getLinkItem()),
              entity.getX(),
              entity.getY() + 0.3,
              entity.getZ(),
              15,
              0.2,
              0.2,
              0.2,
              0.2);
    }
  }

  public static boolean approximatelyZero(double a) {
    return Math.abs(0 - a) < 0.00029146489604938;
  }

  public static void unlinkFromParent(AbstractMinecart entity) {
    if (entity == null) return;
    var following = ((LinkableMinecart) entity).linkart$getFollowing();
    if (following == null) return;

    ((LinkableMinecart) following).linkart$setFollower(null);
    ((LinkableMinecart) entity).linkart$setFollowing(null);

    entity.setDeltaMovement(0, 0, 0);

    if (!((LinkableMinecart) entity).linkart$getLinkItem().isEmpty()) {
      entity.spawnAtLocation(
          (ServerLevel) entity.level, ((LinkableMinecart) entity).linkart$getLinkItem());
      spawnChainParticles(entity);
    }

    ((LinkableMinecart) entity).linkart$setLinkItem(ItemStack.EMPTY);
  }

  public static void linkTo(AbstractMinecart minecart, AbstractMinecart to, ItemStack linkingItem) {
    ((LinkableMinecart) minecart).linkart$setFollowing(to);
    ((LinkableMinecart) to).linkart$setFollower(minecart);

    if (!linkingItem.isEmpty()) {
      ItemStack linkStack = linkingItem.copy();
      linkStack.setCount(1);
      ((LinkableMinecart) minecart).linkart$setLinkItem(linkStack);
    }

    LinkartMain.spawnChainParticles(minecart);
  }
}

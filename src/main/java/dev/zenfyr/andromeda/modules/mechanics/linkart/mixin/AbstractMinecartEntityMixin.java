package dev.zenfyr.andromeda.modules.mechanics.linkart.mixin;

import dev.zenfyr.andromeda.common.util.MiscUtil;
import dev.zenfyr.andromeda.modules.mechanics.linkart.LinkableMinecart;
import dev.zenfyr.andromeda.modules.mechanics.linkart.Linkart;
import dev.zenfyr.andromeda.modules.mechanics.linkart.LinkartMain;
import dev.zenfyr.andromeda.modules.mechanics.linkart.LoadingCarts;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartEntityMixin extends Entity implements LinkableMinecart {

  @Unique private double lastMovementLength = 0.0D; // Movement length on previous tick

  @Unique private AbstractMinecart linkart$following;

  @Unique private AbstractMinecart linkart$follower;

  @Unique private UUID linkart$followingUUID;

  @Unique private UUID linkart$followerUUID;

  @Unique private ItemStack linkart$itemStack = ItemStack.EMPTY;

  public AbstractMinecartEntityMixin(EntityType<?> type, Level world) {
    super(type, world);
  }

  @Inject(at = @At("HEAD"), method = "tick")
  private void linkart$tick(CallbackInfo ci) {
    if (level().isClientSide()) return;
    AbstractMinecart cast = (AbstractMinecart) (Object) this;
    if (linkart$getFollowing() == null) return;
    var cfg = level().am$get(Linkart.CONFIG);

    Vec3 pos = position();
    Vec3 pos2 = linkart$getFollowing().position();
    double dist = Math.max(Math.abs(pos.distanceTo(pos2)) - cfg.distance, 0);
    Vec3 vec3d = pos.vectorTo(pos2);
    vec3d = vec3d.scale(cfg.velocityMultiplier);

    // Check if we are on a sharp curve
    Vec3 vel = getDeltaMovement();
    Vec3 vel2 = linkart$getFollowing().getDeltaMovement();
    boolean differentDirection = (vel.length() > 0.15
        && vel2.length() > 0.005
        && vel.normalize().distanceTo(vel2.normalize()) > 1.42
        && pos.distanceTo(pos2) > 0.5);

    if (differentDirection) {
      // Keep ourselves going at same speed if on curve
      dist += cfg.distance;
      vec3d = vel;
    }

    // Calculate new velocity
    vec3d = vec3d.normalize().scale(dist);

    if (dist <= 1) {
      // Go slower (1.0->0.8) the closer (1->0) we are
      setDeltaMovement(vec3d.scale(0.8 + 0.2 * Math.abs(dist)));
    } else if (dist <= cfg.pathfindingDistance) {
      setDeltaMovement(vec3d);
    } else {
      LinkartMain.unlinkFromParent(cast);
    }

    if (cfg.chunkloading) {
      if (linkart$getFollower() != null
          && !LinkartMain.approximatelyZero(this.getDeltaMovement().length())) {
        ((ServerLevel) this.level())
            .getChunkSource()
            .addTicketWithRadius(TicketType.PORTAL, this.chunkPosition(), cfg.chunkloadingRadius);
        LoadingCarts.get(level()).addCart(cast);
      } else {
        LoadingCarts.get(level()).removeCart(cast);
      }
    }
  }

  @Inject(at = @At("HEAD"), method = "push", cancellable = true)
  void onPushAway(Entity entity, CallbackInfo ci) {
    if (!LinkartMain.shouldCollide(this, entity)) ci.cancel();
  }

  @Inject(at = @At("RETURN"), method = "addAdditionalSaveData")
  private void linkart$write(ValueOutput output, CallbackInfo ci) {
    output.storeNullable("LK-Following", MiscUtil.UUID_CODEC, linkart$followingUUID);
    output.storeNullable("LK-Follower", MiscUtil.UUID_CODEC, linkart$followerUUID);
    output.store("LK-ItemStack", ItemStack.OPTIONAL_CODEC, this.linkart$itemStack);
  }

  @Inject(at = @At("RETURN"), method = "readAdditionalSaveData")
  private void linkart$read(ValueInput input, CallbackInfo ci) {
    linkart$followingUUID = input.read("LK-Following", MiscUtil.UUID_CODEC).orElse(null);
    linkart$followerUUID = input.read("LK-Follower", MiscUtil.UUID_CODEC).orElse(null);
    linkart$itemStack =
        input.read("LK-ItemStack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
  }

  @Override
  public AbstractMinecart linkart$getFollowing() {
    if (linkart$following == null && linkart$followingUUID != null) {
      linkart$following = (AbstractMinecart) this.level().getEntity(linkart$followingUUID);
    }
    return linkart$following;
  }

  @Override
  public void linkart$setFollowing(AbstractMinecart following) {
    this.linkart$following = following;
    this.linkart$followingUUID = following != null ? following.getUUID() : null;
  }

  @Override
  public AbstractMinecart linkart$getFollower() {
    if (linkart$follower == null && linkart$followerUUID != null) {
      linkart$follower = (AbstractMinecart) this.level().getEntity(linkart$followerUUID);
    }
    return linkart$follower;
  }

  @Override
  public void linkart$setFollower(AbstractMinecart follower) {
    this.linkart$follower = follower;
    this.linkart$followerUUID = follower != null ? follower.getUUID() : null;
  }

  @Override
  public ItemStack linkart$getLinkItem() {
    return linkart$itemStack;
  }

  @Override
  public void linkart$setLinkItem(ItemStack linkItem) {
    this.linkart$itemStack = linkItem == null ? ItemStack.EMPTY : linkItem;
  }

  @Override
  public double linkart$lastMovementLength() {
    return this.lastMovementLength;
  }

  @Override
  public void linkart$lastMovementLength(double len) {
    this.lastMovementLength = len;
  }
}

package dev.zenfyr.andromeda.modules.entities.boats.entities;

import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.modules.entities.boats.packets.ExplodeBoatC2SPayload;
import dev.zenfyr.pulsar.util.SupportUtil;
import dev.zenfyr.pulsar.util.TextUtil;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class TNTBoatEntity extends BoatEntityWithBlock {
  public static final Identifier EXPLODE_BOAT_ON_SERVER = Andromeda.id("explode_boat_on_server");
  public int fuseTicks = -1;

  public TNTBoatEntity(
      EntityType<? extends AbstractBoat> entityType,
      Level world,
      BoatRideHeightFactory rideHeight,
      Supplier<Item> dropItem) {
    super(entityType, world, rideHeight, dropItem);
  }

  @Override
  protected Component getTypeName() {
    return TextUtil.translatable("entity.andromeda.tnt_boat");
  }

  private final Runnable explode = SupportUtil.support(
      EnvType.CLIENT,
      () -> () -> ClientPlayNetworking.send(new ExplodeBoatC2SPayload(this.getUUID())),
      () -> this::explode);

  @Override
  public void tick() {
    if (this.fuseTicks > 0) {
      --this.fuseTicks;
      Vec3 vec3d = new Vec3(-0.55, 0.0, 0.0).yRot(-this.getYRot() * PIby180 - PIby2);
      level.addParticle(
          ParticleTypes.SMOKE,
          this.getX() + vec3d.x,
          this.getY() + 0.8,
          this.getZ() + vec3d.z,
          -(this.getDeltaMovement().x * 0.3),
          0.08,
          -(this.getDeltaMovement().z * 0.3));
    } else if (this.fuseTicks == 0) {
      this.explode();
    }

    if (this.horizontalCollision) {
      if ((this.getFirstPassenger() instanceof Player)) {
        this.explode.run();
      } else {
        this.explode();
      }
    }
    super.tick();
  }

  @Override
  public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
    Entity entity = source.getDirectEntity();

    if (entity instanceof AbstractArrow persistentProjectileEntity
        && persistentProjectileEntity.isOnFire()) {
      this.setFuse();
      return false;
    }
    if (source.is(DamageTypeTags.IS_FIRE)) {
      this.setFuse();
      return false;
    }

    if (source.is(DamageTypeTags.IS_EXPLOSION)) {
      this.setFuse();
      return false;
    }

    if (this.isInvulnerableToBase(source)) {
      return false;
    } else if (!this.isRemoved()) {
      this.setHurtDir(-this.getHurtDir());
      this.setHurtTime(10);
      this.setDamage(this.getDamage() + amount * 10.0F);
      this.markHurt();
      this.gameEvent(GameEvent.ENTITY_DAMAGE, source.getEntity());
      boolean bl = source.getEntity() instanceof Player player && player.getAbilities().instabuild;
      if (bl) {
        this.discard();
        return false;
      }
      if (this.getDamage() > 40.0F) {
        this.explode();
      }
    }
    return false;
  }

  @Override
  public InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
    ItemStack stack = player.getItemInHand(hand);
    if (hand == InteractionHand.MAIN_HAND
        && (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE))) {
      this.setFuse();
      if (!player.isCreative()) {
        if (stack.is(Items.FLINT_AND_STEEL)) {
          stack.hurtAndBreak(1, player, hand);
        } else {
          stack.shrink(1);
        }
      }
      return InteractionResult.SUCCESS;
    }
    return super.interact(player, hand, location);
  }

  @Override
  protected void readAdditionalSaveData(ValueInput nbt) {
    super.readAdditionalSaveData(nbt);
    if (nbt.contains("AM-TNTFuse")) {
      this.fuseTicks = nbt.getIntOr("AM-TNTFuse", -1);
    }
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput nbt) {
    super.addAdditionalSaveData(nbt);
    nbt.putInt("AM-TNTFuse", this.fuseTicks);
  }

  public void setFuse() {
    if (this.fuseTicks == -1) {
      this.fuseTicks = 50 + level.getRandom().nextInt(20);
      if (!level.isClientSide()) {
        level.playSound(null, this, SoundEvents.TNT_PRIMED, SoundSource.HOSTILE, 1F, 1F);
      }
    }
  }

  public void explode() {
    if (!this.level.isClientSide()) {
      this.discard();
      this.level.explode(
          this, this.getX(), this.getY(), this.getZ(), 4.0F, Level.ExplosionInteraction.TNT);
    }
  }
}

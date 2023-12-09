package me.melontini.andromeda.modules.entities.boats.entities;

import me.melontini.andromeda.modules.entities.boats.BoatEntities;
import me.melontini.andromeda.modules.entities.boats.BoatItems;
import me.melontini.andromeda.util.AndromedaPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class TNTBoatEntity extends BoatEntityWithBlock {
    public int fuseTicks = -1;

    public TNTBoatEntity(EntityType<? extends BoatEntity> entityType, World world) {
        super(entityType, world);
    }

    public TNTBoatEntity(World world, double x, double y, double z) {
        this(BoatEntities.BOAT_WITH_TNT.orThrow(), world);
        this.setPosition(x, y, z);
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
    }

    @Override
    public void tick() {
        if (this.fuseTicks > 0) {
            --this.fuseTicks;
            Vec3d vec3d = new Vec3d(-0.55, 0.0, 0.0).rotateY(-this.getYaw() * PIby180 - PIby2);
            world.addParticle(ParticleTypes.SMOKE, this.getX() + vec3d.x, this.getY() + 0.8, this.getZ() + vec3d.z, -(this.getVelocity().x * 0.3), 0.08, -(this.getVelocity().z * 0.3));
        } else if (this.fuseTicks == 0) {
            this.explode();
        }

        if (this.horizontalCollision) {
            if ((this.getFirstPassenger() instanceof PlayerEntity)) {
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeUuid(this.getUuid());
                ClientPlayNetworking.send(AndromedaPackets.EXPLODE_BOAT_ON_SERVER, buf);
            } else {
                this.explode();
            }
        }
        super.tick();
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        Entity entity = source.getSource();

        if (entity instanceof PersistentProjectileEntity persistentProjectileEntity && persistentProjectileEntity.isOnFire()) {
            this.setFuse();
            return false;
        }
        if (source.isFire()) {
            this.setFuse();
            return false;
        }

        if (source.isExplosive()) {
            this.setFuse();
            return false;
        }

        if (this.isInvulnerableTo(source)) {
            return false;
        } else if (!this.world.isClient && !this.isRemoved()) {
            this.setDamageWobbleSide(-this.getDamageWobbleSide());
            this.setDamageWobbleTicks(10);
            this.setDamageWobbleStrength(this.getDamageWobbleStrength() + amount * 10.0F);
            this.scheduleVelocityUpdate();
            this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
            boolean bl = source.getAttacker() instanceof PlayerEntity && ((PlayerEntity) source.getAttacker()).getAbilities().creativeMode;
            if (bl) {
                this.discard();
                return false;
            }
            if (this.getDamageWobbleStrength() > 40.0F) {
                this.explode();
            }
        }
        return false;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (hand == Hand.MAIN_HAND && (stack.isOf(Items.FLINT_AND_STEEL) || stack.isOf(Items.FIRE_CHARGE))) {
            this.setFuse();
            if (!player.isCreative()) {
                if (stack.isOf(Items.FLINT_AND_STEEL)) {
                    stack.damage(1, player, playerx -> playerx.sendToolBreakStatus(hand));
                } else {
                    stack.decrement(1);
                }
            }
            return ActionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    public Item asItem() {
        return Registries.ITEM.get(BoatItems.boatId(this.getVariant(), "tnt"));
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("AM-TNTFuse", 99)) {
            this.fuseTicks = nbt.getInt("AM-TNTFuse");
        }

    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("AM-TNTFuse", this.fuseTicks);
    }

    public void setFuse() {
        if (this.fuseTicks == -1) {
            this.fuseTicks = 50 + Random.create().nextInt(20);
            if (!world.isClient) {
                world.playSoundFromEntity(null, this, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.HOSTILE, 1F, 1F);
            }
        }
    }

    public void explode() {
        if (!this.world.isClient) {
            this.discard();
            this.world.createExplosion(this, this.getX(), this.getY(), this.getZ(), 4.0F, World.ExplosionSourceType.TNT);
        }
    }
}

package dev.zenfyr.andromeda.modules.items.infinite_totem.mixin;

import dev.zenfyr.andromeda.common.util.MiscUtil;
import dev.zenfyr.andromeda.modules.items.infinite_totem.BeaconUtil;
import dev.zenfyr.andromeda.modules.items.infinite_totem.InfiniteTotem;
import dev.zenfyr.andromeda.modules.items.infinite_totem.InfiniteTotemMain;
import dev.zenfyr.andromeda.modules.items.infinite_totem.packets.NotifyClientPayload;
import dev.zenfyr.pulsar.api.util.tuple.Tuple;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
abstract class ItemEntityMixin extends Entity {

  @Shadow
  public abstract void setNeverPickUp();

  @Shadow
  public abstract void setDefaultPickUpDelay();

  @Shadow
  public abstract ItemStack getItem();

  @Unique private static final Tuple<BeaconBlockEntity, Boolean> ANDROMEDA$NULL_BEACON =
      Tuple.of(null, false);

  @Unique private int andromeda$ascensionTicks;

  @Unique private ItemEntity andromeda$itemEntity;

  @Unique private Tuple<BeaconBlockEntity, Boolean> andromeda$beacon = ANDROMEDA$NULL_BEACON;

  public ItemEntityMixin(EntityType<?> type, Level world) {
    super(type, world);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/entity/Entity;tick()V",
              shift = At.Shift.BEFORE),
      method = "tick")
  private void andromeda$tick(CallbackInfo ci) {
    if (this.level().isClientSide()) return;
    if (!this.getItem().is(Items.TOTEM_OF_UNDYING)) return;
    var c = level().am$get(InfiniteTotem.CONFIG);
    if (!c.available || !c.enableAscension) return;

    if (tickCount % 35 == 0 && andromeda$ascensionTicks == 0) {
      if (!andromeda$beaconCheck()) {
        this.setDefaultPickUpDelay();
        if (andromeda$itemEntity != null) andromeda$itemEntity.setDefaultPickUpDelay();
      }
    }

    if (andromeda$beacon.left() != null && andromeda$beacon.right()) {
      if (andromeda$itemEntity == null) {
        if (andromeda$ascensionTicks > 0) --andromeda$ascensionTicks;

        if (tickCount % 10 == 0) {
          Optional<ItemEntity> optional = level()
              .getEntitiesOfClass(
                  ItemEntity.class,
                  getBoundingBox().inflate(0.5),
                  itemEntity -> itemEntity.getItem().is(Items.NETHER_STAR)
                      && toMixin(itemEntity).andromeda$itemEntity == null)
              .stream()
              .findAny();

          if (optional.isPresent()) {
            andromeda$itemEntity = optional.get();
            toMixin(andromeda$itemEntity).andromeda$itemEntity = (ItemEntity) (Object) this;

            ItemStack targetStack = andromeda$itemEntity.getItem();
            int count = targetStack.getCount() - 1;
            if (count > 0) {
              ItemStack newStack = targetStack.copy();
              newStack.setCount(count);
              targetStack.setCount(1);

              andromeda$itemEntity.setItem(targetStack);

              ItemEntity entity = new ItemEntity(
                  level(),
                  andromeda$itemEntity.getX(),
                  andromeda$itemEntity.getY(),
                  andromeda$itemEntity.getZ(),
                  newStack);
              level().addFreshEntity(entity);

              var payload = new NotifyClientPayload(andromeda$itemEntity.getId(), targetStack);
              for (ServerPlayer serverPlayerEntity : PlayerLookup.tracking(this)) {
                ServerPlayNetworking.send(serverPlayerEntity, payload);
              }
            }

            andromeda$itemEntity.setNeverPickUp();
            this.setNeverPickUp();
          }
        }
      } else {
        if (andromeda$beaconCheck()) {
          andromeda$ascensionTicks++;

          MiscUtil.crudeSetVelocity(this, 0, 0.07, 0);
          MiscUtil.crudeSetVelocity(andromeda$itemEntity, 0, 0.07, 0);

          if (andromeda$ascensionTicks == 180) {
            andromeda$ascensionTicks = 0;

            ((ServerLevel) level())
                .sendParticles(
                    ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 15, 0, 0, 0, 0.4);

            ItemEntity entity = new ItemEntity(
                level(),
                this.getX(),
                this.getY(),
                this.getZ(),
                new ItemStack(InfiniteTotemMain.INFINITE_TOTEM.orThrow()));
            this.discard();
            andromeda$itemEntity.discard();
            level().addFreshEntity(entity);
          }
        } else {
          this.setDefaultPickUpDelay();
          andromeda$itemEntity.setDefaultPickUpDelay();
          toMixin(andromeda$itemEntity).andromeda$itemEntity = null;

          andromeda$itemEntity = null;
        }
      }
    }
  }

  @Unique private static ItemEntityMixin toMixin(ItemEntity entity) {
    return ((ItemEntityMixin) (Object) entity);
  }

  @Unique private boolean andromeda$beaconCheck() {
    BlockEntity entity = level()
        .getBlockEntity(blockPosition()
            .atY(level()
                    .getHeight(
                        Heightmap.Types.WORLD_SURFACE,
                        blockPosition().getX(),
                        blockPosition().getZ())
                - 1));
    if (entity instanceof BeaconBlockEntity beaconBlock) {
      this.andromeda$beacon =
          Tuple.of(beaconBlock, BeaconUtil.matchesPattern(level(), beaconBlock.getBlockPos()));
      return true;
    } else {
      this.andromeda$beacon = ANDROMEDA$NULL_BEACON;
      return false;
    }
  }

  @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
  private void andromeda$readNbt(ValueInput valueInput, CallbackInfo ci) {
    this.andromeda$ascensionTicks = valueInput.getIntOr("AM-Ascension", 0);
  }

  @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
  private void andromeda$writeNbt(ValueOutput valueOutput, CallbackInfo ci) {
    valueOutput.putInt("AM-Ascension", this.andromeda$ascensionTicks);
  }
}

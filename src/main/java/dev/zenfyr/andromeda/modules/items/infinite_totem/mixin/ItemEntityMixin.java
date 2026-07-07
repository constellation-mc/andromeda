package dev.zenfyr.andromeda.modules.items.infinite_totem.mixin;

import dev.zenfyr.andromeda.modules.items.infinite_totem.InfiniteTotemDuck;
import dev.zenfyr.andromeda.modules.items.infinite_totem.InfiniteTotemMain;
import dev.zenfyr.andromeda.modules.items.infinite_totem.client.InfiniteTotemClient;
import dev.zenfyr.pulsar.api.util.tuple.Tuple;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
abstract class ItemEntityMixin extends Entity implements InfiniteTotemDuck {

  @Unique private final AtomicInteger andromeda$ascensionTicks = new AtomicInteger(0);

  @Unique private ItemEntity andromeda$itemEntity;

  @Unique private Tuple<BeaconBlockEntity, Boolean> andromeda$beacon = InfiniteTotemMain.NULL_BEACON;

  public ItemEntityMixin(EntityType<?> type, Level level) {
    super(type, level);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target = "Lnet/minecraft/world/entity/Entity;tick()V",
              shift = At.Shift.BEFORE),
      method = "tick")
  private void andromeda$tick(CallbackInfo ci) {
    ItemEntity self = (ItemEntity) (Object) this;
    if (!self.getItem().is(Items.TOTEM_OF_UNDYING)) return;

    if (this.level().isClientSide()) {
      InfiniteTotemClient.clientTotemItemTick(self);
    } else {
      InfiniteTotemMain.serverTotemItemTick((ServerLevel) level(), self);
    }
  }

  @Override
  public AtomicInteger andromeda$ascensionTicks() {
    return this.andromeda$ascensionTicks;
  }

  @Override
  public ItemEntity andromeda$ascensionItem() {
    return this.andromeda$itemEntity;
  }

  @Override
  public void andromeda$ascensionItem(ItemEntity item) {
    this.andromeda$itemEntity = item;
  }

  @Override
  public Tuple<BeaconBlockEntity, Boolean> andromeda$beacon() {
    return this.andromeda$beacon;
  }

  @Override
  public void andromeda$beacon(Tuple<BeaconBlockEntity, Boolean> beacon) {
    this.andromeda$beacon = beacon;
  }

  @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
  private void andromeda$readNbt(ValueInput input, CallbackInfo ci) {
    this.andromeda$ascensionTicks.set(input.getIntOr("AM-Ascension", 0));
  }

  @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
  private void andromeda$writeNbt(ValueOutput output, CallbackInfo ci) {
    output.putInt("AM-Ascension", this.andromeda$ascensionTicks.get());
  }
}

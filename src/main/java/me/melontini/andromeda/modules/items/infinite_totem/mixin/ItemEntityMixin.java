package me.melontini.andromeda.modules.items.infinite_totem.mixin;

import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.common.util.WorldUtil;
import me.melontini.andromeda.modules.items.infinite_totem.BeaconUtil;
import me.melontini.andromeda.modules.items.infinite_totem.InfiniteTotem;
import me.melontini.andromeda.modules.items.infinite_totem.Main;
import me.melontini.dark_matter.api.base.util.functions.Memoize;
import me.melontini.dark_matter.api.base.util.tuple.Tuple;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ItemEntity.class)
abstract class ItemEntityMixin extends Entity {

    @Shadow
    public abstract void setPickupDelayInfinite();

    @Shadow
    public abstract void setToDefaultPickupDelay();

    @Shadow public abstract ItemStack getStack();

    @Unique private static final Tuple<BeaconBlockEntity, Boolean> ANDROMEDA$NULL_BEACON = Tuple.of(null, false);
    @Unique private int andromeda$ascensionTicks;
    @Unique private ItemEntity andromeda$itemEntity;
    @Unique private Tuple<BeaconBlockEntity, Boolean> andromeda$beacon = ANDROMEDA$NULL_BEACON;


    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V", shift = At.Shift.BEFORE), method = "tick")
    private void andromeda$tick(CallbackInfo ci) {
        if (this.world.isClient()) return;
        if (!this.getStack().isOf(Items.TOTEM_OF_UNDYING)) return;
        var c = world.am$get(InfiniteTotem.CONFIG);
        var supplier = Memoize.supplier(LootContextUtil.fishing(world, getPos(), getStack()));
        if (!c.available.asBoolean(supplier) || !c.enableAscension.asBoolean(supplier)) return;

        if (age % 35 == 0 && andromeda$ascensionTicks == 0) {
            if (!andromeda$beaconCheck()) {
                this.setToDefaultPickupDelay();
                if (andromeda$itemEntity != null) andromeda$itemEntity.setToDefaultPickupDelay();
            }
        }

        if (andromeda$beacon.left() != null && andromeda$beacon.right()) {
            if (andromeda$itemEntity == null) {
                if (andromeda$ascensionTicks > 0) --andromeda$ascensionTicks;

                if (age % 10 == 0) {
                    Optional<ItemEntity> optional = world.getEntitiesByClass(ItemEntity.class, getBoundingBox().expand(0.5), itemEntity -> itemEntity.getStack().isOf(Items.NETHER_STAR) && toMixin(itemEntity).andromeda$itemEntity == null).stream().findAny();

                    if (optional.isPresent()) {
                        andromeda$itemEntity = optional.get();
                        toMixin(andromeda$itemEntity).andromeda$itemEntity = (ItemEntity) (Object) this;

                        ItemStack targetStack = andromeda$itemEntity.getStack();
                        int count = targetStack.getCount() - 1;
                        if (count > 0) {
                            ItemStack newStack = targetStack.copy();
                            newStack.setCount(count);
                            targetStack.setCount(1);

                            andromeda$itemEntity.setStack(targetStack);

                            ItemEntity entity = new ItemEntity(world, andromeda$itemEntity.getX(), andromeda$itemEntity.getY(), andromeda$itemEntity.getZ(), newStack);
                            world.spawnEntity(entity);

                            PacketByteBuf buf = PacketByteBufs.create()
                                    .writeVarInt(andromeda$itemEntity.getId())
                                    .writeItemStack(targetStack);
                            for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.tracking(this)) {
                                ServerPlayNetworking.send(serverPlayerEntity, Main.NOTIFY_CLIENT, buf);
                            }
                        }

                        andromeda$itemEntity.setPickupDelayInfinite();
                        this.setPickupDelayInfinite();
                    }
                }
            } else {
                if (andromeda$beaconCheck()) {
                    andromeda$ascensionTicks++;

                    WorldUtil.crudeSetVelocity(this, 0, 0.07, 0);
                    WorldUtil.crudeSetVelocity(andromeda$itemEntity, 0, 0.07, 0);

                    if (andromeda$ascensionTicks == 180) {
                        andromeda$ascensionTicks = 0;

                        ((ServerWorld) world).spawnParticles(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 15, 0, 0, 0, 0.4);

                        ItemEntity entity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), new ItemStack(Main.INFINITE_TOTEM.orThrow()));
                        this.discard();
                        andromeda$itemEntity.discard();
                        world.spawnEntity(entity);
                    }
                } else {
                    this.setToDefaultPickupDelay();
                    andromeda$itemEntity.setToDefaultPickupDelay();
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
        BlockEntity entity = world.getBlockEntity(new BlockPos((int) getX(), world.getTopY(Heightmap.Type.WORLD_SURFACE, getBlockPos().getX(), getBlockPos().getZ()) - 1, (int) getZ()));
        if (entity instanceof BeaconBlockEntity beaconBlock) {
            this.andromeda$beacon = Tuple.of(beaconBlock, BeaconUtil.matchesPattern(world, beaconBlock.getPos()));
            return true;
        } else {
            this.andromeda$beacon = ANDROMEDA$NULL_BEACON;
            return false;
        }
    }

    @Inject(at = @At("TAIL"), method = "readCustomDataFromNbt")
    private void andromeda$readNbt(NbtCompound nbt, CallbackInfo ci) {
        this.andromeda$ascensionTicks = nbt.getInt("AM-Ascension");
    }

    @Inject(at = @At("TAIL"), method = "writeCustomDataToNbt")
    private void andromeda$writeNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("AM-Ascension", this.andromeda$ascensionTicks);
    }
}

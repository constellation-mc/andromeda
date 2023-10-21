package me.melontini.andromeda.mixin.items.infinite_totem;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.registries.ItemRegistry;
import me.melontini.andromeda.util.BlockUtil;
import me.melontini.andromeda.util.WorldUtil;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.api.base.util.classes.Tuple;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static me.melontini.andromeda.util.CommonValues.MODID;

@Mixin(ItemEntity.class)
@MixinRelatedConfigOption({"totemSettings.enableInfiniteTotem", "totemSettings.enableTotemAscension"})
abstract class ItemEntityMixin extends Entity {
    @Unique
    private static final Set<ItemEntity> ANDROMEDA$ITEMS = new HashSet<>();
    @Unique
    private static final Tuple<BeaconBlockEntity, Integer> ANDROMEDA$NULL_BEACON = Tuple.of(null, 0);
    @Shadow
    @Final
    private static TrackedData<ItemStack> STACK;

    @Unique
    private final List<Block> beaconBlocks = List.of(Blocks.DIAMOND_BLOCK, Blocks.NETHERITE_BLOCK);
    @Unique
    private int andromeda$ascensionTicks;
    @Unique
    private ItemEntity andromeda$itemEntity;
    @Unique
    private Tuple<BeaconBlockEntity, Integer> andromeda$beacon = ANDROMEDA$NULL_BEACON;


    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract void setPickupDelayInfinite();

    @Shadow
    public abstract void setToDefaultPickupDelay();

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V", shift = At.Shift.BEFORE), method = "tick")
    private void andromeda$tick(CallbackInfo ci) {
        if (!Config.get().totemSettings.enableTotemAscension || !Config.get().totemSettings.enableInfiniteTotem)
            return;
        if (!this.dataTracker.get(STACK).isOf(Items.TOTEM_OF_UNDYING)) return;

        if (age % 35 == 0 && andromeda$ascensionTicks == 0) {
            if (!andromeda$beaconCheck()) {
                this.setToDefaultPickupDelay();
                if (andromeda$itemEntity != null) andromeda$itemEntity.setToDefaultPickupDelay();
            }
        }

        if (andromeda$beacon.left() != null && andromeda$beacon.right() >= 4) {
            if (!world.isClient) {
                if (andromeda$itemEntity == null) {
                    if (andromeda$ascensionTicks > 0) --andromeda$ascensionTicks;

                    if (age % 10 == 0) {
                        Optional<ItemEntity> optional = world.getEntitiesByClass(ItemEntity.class, getBoundingBox().expand(0.5), itemEntity -> itemEntity.getDataTracker().get(STACK).isOf(Items.NETHER_STAR) && !ANDROMEDA$ITEMS.contains(itemEntity)).stream().findAny();

                        if (optional.isPresent()) {
                            andromeda$itemEntity = optional.get();

                            if (ANDROMEDA$ITEMS.contains(andromeda$itemEntity)) {
                                andromeda$itemEntity = null;
                                return;
                            }

                            ItemStack targetStack = andromeda$itemEntity.getDataTracker().get(STACK);
                            int count = targetStack.getCount() - 1;
                            if (count > 0) {
                                ItemStack newStack = targetStack.copy();
                                newStack.setCount(count);
                                targetStack.setCount(1);

                                andromeda$itemEntity.getDataTracker().set(STACK, targetStack);

                                ItemEntity entity = new ItemEntity(world, andromeda$itemEntity.getX(), andromeda$itemEntity.getY(), andromeda$itemEntity.getZ(), newStack);
                                world.spawnEntity(entity);

                                PacketByteBuf buf = PacketByteBufs.create();
                                buf.writeVarInt(andromeda$itemEntity.getId());
                                buf.writeItemStack(targetStack);
                                for (ServerPlayerEntity serverPlayerEntity : PlayerLookup.tracking(this)) {
                                    ServerPlayNetworking.send(serverPlayerEntity, new Identifier(MODID, "notify_client_about_stuff_please"), buf);
                                }
                            }

                            ANDROMEDA$ITEMS.add(andromeda$itemEntity);
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

                            ItemEntity entity = new ItemEntity(world, this.getX(), this.getY(), this.getZ(), new ItemStack(ItemRegistry.get().INFINITE_TOTEM));
                            this.discard();
                            andromeda$itemEntity.discard();
                            world.spawnEntity(entity);
                        }
                    } else {
                        this.setToDefaultPickupDelay();
                        andromeda$itemEntity.setToDefaultPickupDelay();

                        andromeda$itemEntity = null;
                    }
                }
            }
        }
    }

    @Unique
    private boolean andromeda$beaconCheck() {
        BlockEntity entity = world.getBlockEntity(new BlockPos(getX(), world.getTopY(Heightmap.Type.WORLD_SURFACE, getBlockPos().getX(), getBlockPos().getZ()) - 1, getZ()));
        if (entity instanceof BeaconBlockEntity beaconBlock) {
            this.andromeda$beacon = Tuple.of(beaconBlock, BlockUtil.getLevelFromBlocks(world, beaconBlock.getPos(), beaconBlocks));
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

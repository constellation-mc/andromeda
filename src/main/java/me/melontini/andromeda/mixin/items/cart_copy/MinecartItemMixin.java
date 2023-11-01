package me.melontini.andromeda.mixin.items.cart_copy;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.registries.ItemRegistry;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.minecraft.data.NbtBuilder;
import me.melontini.dark_matter.api.minecraft.data.NbtUtil;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(MinecartItem.class)
@Feature("minecartBlockPicking")
abstract class MinecartItemMixin extends Item {

    @Shadow @Final public AbstractMinecartEntity.Type type;

    public MinecartItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
    public void andromeda$useOnStuff(@NotNull ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        ItemStack stack = context.getStack();
        PlayerEntity player = context.getPlayer();
        if (player == null) return;

        if (state.isIn(BlockTags.RAILS)) {
            RailShape railShape = state.getBlock() instanceof AbstractRailBlock ? state.get(((AbstractRailBlock) state.getBlock()).getShapeProperty()) : RailShape.NORTH_SOUTH;
            double d = 0.0;
            if (railShape.isAscending()) d = 0.5;
            if (stack.getItem() == Items.CHEST_MINECART) {
                if (!world.isClient()) {
                    AbstractMinecartEntity abstractMinecartEntity = AbstractMinecartEntity.create(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.0625 + d, (double) pos.getZ() + 0.5, this.type);
                    ChestMinecartEntity chestMinecart = (ChestMinecartEntity) abstractMinecartEntity;

                    NbtUtil.readInventoryFromNbt(stack.getNbt(), chestMinecart);
                    if (stack.hasCustomName()) chestMinecart.setCustomName(stack.getName());

                    world.spawnEntity(chestMinecart);
                }

                if (!player.isCreative()) stack.decrement(1);
                cir.setReturnValue(ActionResult.success(world.isClient()));
                return;
            }
            if (stack.getItem() == Items.HOPPER_MINECART) {
                if (!world.isClient()) {
                    AbstractMinecartEntity abstractMinecartEntity = AbstractMinecartEntity.create(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.0625 + d, (double) pos.getZ() + 0.5, this.type);
                    HopperMinecartEntity hopperMinecart = (HopperMinecartEntity) abstractMinecartEntity;

                    NbtUtil.readInventoryFromNbt(stack.getNbt(), hopperMinecart);
                    if (stack.hasCustomName()) hopperMinecart.setCustomName(stack.getName());

                    world.spawnEntity(hopperMinecart);
                }

                if (!player.isCreative()) stack.decrement(1);
                cir.setReturnValue(ActionResult.success(world.isClient()));
                return;
            }
            if (stack.getItem() == Items.FURNACE_MINECART) {
                if (!world.isClient()) {
                    AbstractMinecartEntity abstractMinecartEntity = AbstractMinecartEntity.create(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.0625 + d, (double) pos.getZ() + 0.5, this.type);
                    FurnaceMinecartEntity furnaceMinecart = (FurnaceMinecartEntity) abstractMinecartEntity;

                    furnaceMinecart.fuel = NbtUtil.getInt(stack.getNbt(), "Fuel", 0, Config.get().maxFurnaceMinecartFuel);
                    furnaceMinecart.interact(player, player.getActiveHand());
                    if (stack.hasCustomName()) furnaceMinecart.setCustomName(stack.getName());

                    world.spawnEntity(furnaceMinecart);
                }

                if (!player.isCreative()) stack.decrement(1);
                cir.setReturnValue(ActionResult.success(world.isClient()));
                return;
            }
        }

        if (Config.get().minecartBlockPicking && player.isSneaking()) {
            if (stack.getItem() != Items.MINECART) return;

            if (state.isOf(Blocks.CHEST)) {
                if (!world.isClient()) {
                    ChestBlockEntity chestBlockEntity = (ChestBlockEntity) MakeSure.notNull(world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
                    if (!player.isCreative()) stack.decrement(1);
                    ItemStack chestMinecart = new ItemStack(Items.CHEST_MINECART, 1);

                    chestMinecart.setNbt(NbtUtil.writeInventoryToNbt(new NbtCompound(), chestBlockEntity));

                    player.getInventory().offerOrDrop(chestMinecart);
                    chestBlockEntity.inventory.clear();
                    world.breakBlock(pos, false);
                }
                cir.setReturnValue(ActionResult.success(world.isClient()));
                return;
            }
            if (state.isOf(Blocks.SPAWNER) && Config.get().minecartSpawnerPicking) {
                if (!world.isClient()) {
                    MobSpawnerBlockEntity mobSpawnerBlockEntity = (MobSpawnerBlockEntity) MakeSure.notNull(world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
                    if (!player.isCreative()) stack.decrement(1);
                    ItemStack spawnerMinecart = new ItemStack(ItemRegistry.get().SPAWNER_MINECART.get(), 1);

                    spawnerMinecart.setNbt(NbtBuilder.create().putString("Entity", String.valueOf(andromeda$getEntityId(mobSpawnerBlockEntity))).build());

                    player.getInventory().offerOrDrop(spawnerMinecart);
                    world.breakBlock(pos, false);
                }
                cir.setReturnValue(ActionResult.success(world.isClient()));
                return;
            }
            if (state.isOf(Blocks.TNT)) {
                if (!world.isClient()) {
                    if (!player.isCreative()) stack.decrement(1);
                    ItemStack tntMinecart = new ItemStack(Items.TNT_MINECART, 1);

                    player.getInventory().offerOrDrop(tntMinecart);
                    world.breakBlock(pos, false);
                }
                cir.setReturnValue(ActionResult.success(world.isClient()));
                return;
            }
            if (state.isOf(Blocks.FURNACE)) {
                if (!world.isClient()) {
                    if (!player.isCreative()) stack.decrement(1);
                    AbstractFurnaceBlockEntity furnaceBlock = (AbstractFurnaceBlockEntity) MakeSure.notNull(world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
                    ItemStack furnaceMinecart = new ItemStack(Items.FURNACE_MINECART, 1);
                    //2.25
                    furnaceMinecart.setNbt(NbtBuilder.create().putInt("Fuel", (int) (furnaceBlock.burnTime * 2.25)).build());

                    player.getInventory().offerOrDrop(furnaceMinecart);
                    world.breakBlock(pos, false);
                }
                cir.setReturnValue(ActionResult.success(world.isClient()));
                return;
            }
            if (state.isOf(Blocks.HOPPER)) {
                if (!world.isClient()) {
                    HopperBlockEntity hopperBlockEntity = (HopperBlockEntity) MakeSure.notNull(world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));
                    if (!player.isCreative()) stack.decrement(1);
                    ItemStack hopperMinecart = new ItemStack(Items.HOPPER_MINECART, 1);

                    hopperMinecart.setNbt(NbtUtil.writeInventoryToNbt(new NbtCompound(), hopperBlockEntity));

                    player.getInventory().offerOrDrop(hopperMinecart);
                    hopperBlockEntity.inventory.clear();
                    world.breakBlock(pos, false);
                }
                cir.setReturnValue(ActionResult.success(world.isClient()));
                return;
            }
            if (state.isOf(Blocks.NOTE_BLOCK) && Config.get().newMinecarts.isNoteBlockMinecartOn) {
                if (!world.isClient()) {
                    NoteBlock noteBlock = (NoteBlock) state.getBlock();
                    if (!player.isCreative()) stack.decrement(1);
                    int noteProp = noteBlock.getStateWithProperties(state).get(Properties.NOTE);
                    ItemStack noteBlockMinecart = new ItemStack(ItemRegistry.get().NOTE_BLOCK_MINECART.get());

                    noteBlockMinecart.setNbt(NbtBuilder.create().putInt("Note", noteProp).build());

                    player.getInventory().offerOrDrop(noteBlockMinecart);
                    world.breakBlock(pos, false);
                }
                cir.setReturnValue(ActionResult.success(world.isClient()));
                return;
            }
            if (state.isOf(Blocks.JUKEBOX) && Config.get().newMinecarts.isJukeboxMinecartOn) {
                if (!world.isClient()) {
                    if (!player.isCreative()) stack.decrement(1);
                    JukeboxBlockEntity jukeboxBlockEntity = (JukeboxBlockEntity) MakeSure.notNull(world.getBlockEntity(pos), "Block has no block entity. %s".formatted(pos));

                    ItemStack record = jukeboxBlockEntity.getStack(0);
                    ItemStack jukeboxMinecart = new ItemStack(ItemRegistry.get().JUKEBOX_MINECART.get());

                    if (!record.isEmpty()) {
                        world.syncWorldEvent(WorldEvents.JUKEBOX_STARTS_PLAYING, pos, 0);
                        jukeboxMinecart.setNbt(NbtBuilder.create().put("Items", record.writeNbt(new NbtCompound())).build());
                    }

                    player.getInventory().offerOrDrop(jukeboxMinecart);
                    jukeboxBlockEntity.clear();
                    world.breakBlock(pos, false);
                }
                cir.setReturnValue(ActionResult.success(world.isClient()));
                return;
            }
            if (state.isOf(Blocks.ANVIL) && Config.get().newMinecarts.isAnvilMinecartOn) {
                if (!world.isClient()) {
                    if (!player.isCreative()) stack.decrement(1);
                    ItemStack anvilMinecart = new ItemStack(ItemRegistry.get().ANVIL_MINECART.get());
                    player.getInventory().offerOrDrop(anvilMinecart);
                    world.breakBlock(pos, false);
                }
                cir.setReturnValue(ActionResult.success(world.isClient()));
            }
        }
    }

    @Nullable
    @Unique
    private Identifier andromeda$getEntityId(MobSpawnerBlockEntity mobSpawnerBlockEntity) {
        String identifier = mobSpawnerBlockEntity.getLogic().spawnEntry.getNbt().getString("id");

        try {
            return StringUtils.isEmpty(identifier) ? Registries.ENTITY_TYPE.getDefaultId() : new Identifier(identifier);
        } catch (InvalidIdentifierException e) {
            BlockPos blockPos = mobSpawnerBlockEntity.getPos();
            AndromedaLog.error(String.format("Invalid entity id '%s' at spawner %s:[%s,%s,%s]", identifier, Objects.requireNonNull(mobSpawnerBlockEntity.getWorld()).getRegistryKey().getValue(), blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            return Registries.ENTITY_TYPE.getDefaultId();
        }
    }
}

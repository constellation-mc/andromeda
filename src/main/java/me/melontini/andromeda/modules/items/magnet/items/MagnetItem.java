package me.melontini.andromeda.modules.items.magnet.items;

import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.BundleTooltipData;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class MagnetItem extends Item {

    public MagnetItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType == ClickType.RIGHT) {
            ItemStack itemStack = slot.getStack();
            if (itemStack.isEmpty()) {
                removeFirst(stack);
                this.playRemoveOneSound(player);
            } else {
                addFirst(stack, itemStack);
                Support.run(EnvType.CLIENT, () -> () -> itemParticles(itemStack, player));
                this.playInsertSound(player);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT) {
            if (otherStack.isEmpty()) {
                removeFirst(stack);
                this.playRemoveOneSound(player);
            } else {
                addFirst(stack, otherStack);
                itemParticles(otherStack, player);
                this.playInsertSound(player);
            }
            return true;
        }
        if (clickType == ClickType.LEFT) {
            if (otherStack.isOf(Items.HEART_OF_THE_SEA)) {
                if (incrementLevel(stack)) {
                    otherStack.decrement(1);
                    Support.run(EnvType.CLIENT, () -> () -> upgradeParticles(player));
                    playUpgradeSound(player);
                }
                return true;
            }
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    private static void upgradeParticles(PlayerEntity player) {
        if (player.world.isClient()) {
            var client = MinecraftClient.getInstance();
            int x = (int) (client.mouse.getX() * (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth());
            int y = (int) (client.mouse.getY() * (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight());
            ScreenParticleHelper.addScreenParticles(ParticleTypes.END_ROD,
                    x, y, 0.5, 0.5, 0.07, 7);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void itemParticles(ItemStack stack, PlayerEntity player) {
        if (player.world.isClient()) {
            var client = MinecraftClient.getInstance();
            int x = (int) (client.mouse.getX() * (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth());
            int y = (int) (client.mouse.getY() * (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight());
            ScreenParticleHelper.addScreenParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, stack),
                    x, y, 0.5, 0.5, 0.1, 7);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!world.isClient()) {
            if (entity instanceof LivingEntity pe) { //selected doesn't account for offhand
                if (!ItemStack.areItemsEqual(stack, pe.getStackInHand(Hand.MAIN_HAND)) && !ItemStack.areItemsEqual(stack, pe.getStackInHand(Hand.OFF_HAND)))
                    return;
            } else if (!selected) return;

            Set<Item> magnetables = magnetable(stack);
            int level = getLevel(stack);
            world.getEntitiesByClass(ItemEntity.class, new Box(entity.getBlockPos()).expand(level * 5), ie -> magnetables.contains(ie.getDataTracker().get(ItemEntity.STACK).getItem()))
                    .forEach(ie -> {
                        Vec3d vel = ie.getPos().relativize(entity.getPos()).normalize().multiply(0.05f * level);
                        ie.addVelocity(vel.x, vel.y, vel.z);
                    });
        }
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.of();
        magnetable(stack).forEach(item -> defaultedList.add(item.getDefaultStack()));
        return Optional.of(new BundleTooltipData(defaultedList, Integer.MAX_VALUE));
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(TextUtil.translatable("tooltip.andromeda.magnet.level", getLevel(stack)).formatted(Formatting.GRAY));
    }

    private static boolean incrementLevel(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains("PowerLevel")) nbt.putInt("PowerLevel", 1);
        int level = nbt.getInt("PowerLevel");
        if (level >= 5) return false;
        nbt.putInt("PowerLevel", level + 1);
        return true;
    }

    private static int getLevel(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) {
            if (nbt.contains("PowerLevel"))
                return nbt.getInt("PowerLevel");
        }
        return 1;
    }

    public static void addFirst(ItemStack bundle, ItemStack other) {
        NbtCompound nbt = bundle.getOrCreateNbt();
        if (!nbt.contains("Items")) {
            nbt.put("Items", new NbtList());
        }

        NbtList list = nbt.getList("Items", NbtElement.STRING_TYPE);
        NbtString id = NbtString.of(Registries.ITEM.getId(other.getItem()).toString());
        if (list.contains(id)) return;
        list.add(0, id);
    }

    private static void removeFirst(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (nbt.contains("Items")) {
            NbtList list = nbt.getList("Items", NbtElement.STRING_TYPE);
            if (!list.isEmpty()) {
                list.remove(0);
                if (list.isEmpty()) stack.removeSubNbt("Items");
            }
        }
    }

    private static Set<Item> magnetable(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null) {
            return Collections.emptySet();
        } else {
            NbtList nbtList = nbt.getList("Items", NbtElement.STRING_TYPE);
            return nbtList.stream().map(NbtString.class::cast)
                    .map(s -> Registries.ITEM.get(new Identifier(s.asString())))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    private void playUpgradeSound(Entity entity) {
        entity.playSound(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8F, 0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
    }
}

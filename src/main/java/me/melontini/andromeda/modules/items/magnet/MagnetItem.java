package me.melontini.andromeda.modules.items.magnet;

import static me.melontini.andromeda.common.Andromeda.id;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import me.melontini.andromeda.bootstrap.ModuleManager;
import me.melontini.andromeda.common.util.AndromedaItemGroup;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MagnetItem extends Item {

  public static final Keeper<MagnetItem> MAGNET = Keeper.create();
  private static final BiConsumer<ItemStack, Player> ITEM_PARTICLES =
      Support.support(EnvType.CLIENT, () -> MagnetItem::itemParticles, () -> (stack, player) -> {});
  private static final Consumer<Player> UPGRADE_PARTICLES =
      Support.support(EnvType.CLIENT, () -> MagnetItem::upgradeParticles, () -> stack -> {});

  public MagnetItem(Properties settings) {
    super(settings);
  }

  public static final String LEVEL_KEY = "PowerLevel";

  @Override
  public boolean overrideStackedOnOther(
      ItemStack stack, Slot slot, ClickAction clickType, Player player) {
    if (clickType == ClickAction.SECONDARY) {
      ItemStack itemStack = slot.getItem();
      if (itemStack.isEmpty()) {
        removeFirst(stack);
        this.playRemoveOneSound(player);
      } else {
        addFirst(stack, itemStack);
        ITEM_PARTICLES.accept(itemStack, player);
        this.playInsertSound(player);
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean overrideOtherStackedOnMe(
      ItemStack stack,
      ItemStack otherStack,
      Slot slot,
      ClickAction clickType,
      Player player,
      SlotAccess cursorStackReference) {
    if (clickType == ClickAction.SECONDARY) {
      if (otherStack.isEmpty()) {
        removeFirst(stack);
        this.playRemoveOneSound(player);
      } else {
        addFirst(stack, otherStack);
        ITEM_PARTICLES.accept(otherStack, player);
        this.playInsertSound(player);
      }
      return true;
    }
    if (clickType == ClickAction.PRIMARY) {
      if (otherStack.is(Items.HEART_OF_THE_SEA)) {
        if (incrementLevel(stack)) {
          otherStack.shrink(1);
          UPGRADE_PARTICLES.accept(player);
          playUpgradeSound(player);
        }
        return true;
      }
    }
    return false;
  }

  @Environment(EnvType.CLIENT)
  private static void upgradeParticles(Player player) {
    if (player.level.isClientSide()) {
      var client = Minecraft.getInstance();
      int x = (int) (client.mouseHandler.xpos()
          * (double) client.getWindow().getGuiScaledWidth()
          / (double) client.getWindow().getScreenWidth());
      int y = (int) (client.mouseHandler.ypos()
          * (double) client.getWindow().getGuiScaledHeight()
          / (double) client.getWindow().getScreenHeight());
      ScreenParticleHelper.addScreenParticles(ParticleTypes.END_ROD, x, y, 0.5, 0.5, 0.07, 7);
    }
  }

  @Environment(EnvType.CLIENT)
  private static void itemParticles(ItemStack stack, Player player) {
    if (player.level.isClientSide()) {
      var client = Minecraft.getInstance();
      int x = (int) (client.mouseHandler.xpos()
          * (double) client.getWindow().getGuiScaledWidth()
          / (double) client.getWindow().getScreenWidth());
      int y = (int) (client.mouseHandler.ypos()
          * (double) client.getWindow().getGuiScaledHeight()
          / (double) client.getWindow().getScreenHeight());
      ScreenParticleHelper.addScreenParticles(
          new ItemParticleOption(ParticleTypes.ITEM, stack), x, y, 0.5, 0.5, 0.1, 7);
    }
  }

  @Override
  public void inventoryTick(
      ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
    if (!world.isClientSide()) {
      if (entity instanceof LivingEntity pe) { // selected doesn't account for offhand
        if (!ItemStack.isSameItem(stack, pe.getItemInHand(InteractionHand.MAIN_HAND))
            && !ItemStack.isSameItem(stack, pe.getItemInHand(InteractionHand.OFF_HAND))) return;
      } else if (!selected) return;

      Set<Item> magnetables = magnetable(stack);
      int level = getLevel(stack);
      world
          .getEntitiesOfClass(
              ItemEntity.class,
              new AABB(entity.blockPosition())
                  .inflate(level
                      * world
                          .am$get(Magnet.CONFIG)
                          .rangeMultiplier
                          .asDouble(LootContextBuilder.fishing(
                              world,
                              builder -> builder.origin(entity).tool(stack).thisEntity(entity)))),
              ie -> magnetables.contains(
                  ie.getEntityData().get(ItemEntity.DATA_ITEM).getItem()))
          .forEach(ie -> {
            Vec3 vel = ie.position().vectorTo(entity.position()).normalize().scale(0.05f * level);
            ie.push(vel.x, vel.y, vel.z);
          });
    }
  }

  @Override
  public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
    NonNullList<ItemStack> defaultedList = NonNullList.create();
    magnetable(stack).forEach(item -> defaultedList.add(item.getDefaultInstance()));
    return Optional.of(new BundleTooltip(defaultedList, Integer.MAX_VALUE));
  }

  @Override
  public void appendHoverText(
      ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
    tooltip.add(TextUtil.translatable("tooltip.andromeda.magnet.level", getLevel(stack))
        .withStyle(ChatFormatting.GRAY));
  }

  private static boolean incrementLevel(ItemStack stack) {
    CompoundTag nbt = stack.getOrCreateTag();
    if (!nbt.contains(LEVEL_KEY)) nbt.putInt(LEVEL_KEY, 1);
    int level = nbt.getInt(LEVEL_KEY);
    if (level >= 5) return false;
    nbt.putInt(LEVEL_KEY, level + 1);
    return true;
  }

  private static int getLevel(ItemStack stack) {
    CompoundTag nbt = stack.getTag();
    if (nbt != null) {
      if (nbt.contains(LEVEL_KEY)) return MathUtil.clamp(nbt.getInt(LEVEL_KEY), 0, 5);
    }
    return 1;
  }

  public static void addFirst(ItemStack bundle, ItemStack other) {
    CompoundTag nbt = bundle.getOrCreateTag();
    if (!nbt.contains("Items")) {
      nbt.put("Items", new ListTag());
    }

    ListTag list = nbt.getList("Items", Tag.TAG_STRING);
    StringTag id =
        StringTag.valueOf(BuiltInRegistries.ITEM.getKey(other.getItem()).toString());
    if (list.contains(id)) return;
    list.add(0, id);
  }

  private static void removeFirst(ItemStack stack) {
    CompoundTag nbt = stack.getOrCreateTag();
    if (nbt.contains("Items")) {
      ListTag list = nbt.getList("Items", Tag.TAG_STRING);
      if (!list.isEmpty()) {
        list.remove(0);
        if (list.isEmpty()) stack.removeTagKey("Items");
      }
    }
  }

  private static Set<Item> magnetable(ItemStack stack) {
    CompoundTag nbt = stack.getTag();
    if (nbt == null) {
      return Collections.emptySet();
    } else {
      ListTag nbtList = nbt.getList("Items", Tag.TAG_STRING);
      return nbtList.stream()
          .map(StringTag.class::cast)
          .map(s -> BuiltInRegistries.ITEM.get(new ResourceLocation(s.getAsString())))
          .collect(ImmutableSet.toImmutableSet());
    }
  }

  private void playUpgradeSound(Entity entity) {
    entity.playSound(
        SoundEvents.ENCHANTMENT_TABLE_USE,
        0.8F,
        0.8F + entity.level().getRandom().nextFloat() * 0.4F);
  }

  private void playRemoveOneSound(Entity entity) {
    entity.playSound(
        SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
  }

  private void playInsertSound(Entity entity) {
    entity.playSound(
        SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
  }

  static void init() {
    var module = ModuleManager.get().get(Magnet.class).orElseThrow();

    MagnetItem.MAGNET.init(RegistryUtil.register(
        BuiltInRegistries.ITEM,
        id("magnet"),
        () -> new MagnetItem(new FabricItemSettings().stacksTo(1))));

    AndromedaItemGroup.BUS.listen(acceptor ->
        acceptor.keeper(module, CreativeModeTabs.TOOLS_AND_UTILITIES, MagnetItem.MAGNET));
  }
}

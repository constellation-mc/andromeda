package me.melontini.andromeda.modules.items.magnet;

import static me.melontini.andromeda.common.Andromeda.id;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.With;
import me.melontini.andromeda.common.AndromedaItemGroup;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.BundleTooltipData;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MagnetItem extends Item {

  public static final Keeper<MagnetItem> MAGNET = Keeper.create();
  public static final Keeper<ComponentType<MagnetContents>> COMPONENT_TYPE = Keeper.create();
  private static final BiConsumer<ItemStack, PlayerEntity> ITEM_PARTICLES =
      Support.support(EnvType.CLIENT, () -> MagnetItem::itemParticles, () -> (stack, player) -> {});
  private static final Consumer<PlayerEntity> UPGRADE_PARTICLES =
      Support.support(EnvType.CLIENT, () -> MagnetItem::upgradeParticles, () -> stack -> {});

  public MagnetItem(Settings settings) {
    super(settings);
  }

  public static final String LEVEL_KEY = "PowerLevel";

  @Override
  public boolean onStackClicked(
      ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
    if (clickType == ClickType.RIGHT) {
      ItemStack itemStack = slot.getStack();
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
  public boolean onClicked(
      ItemStack stack,
      ItemStack otherStack,
      Slot slot,
      ClickType clickType,
      PlayerEntity player,
      StackReference cursorStackReference) {
    if (clickType == ClickType.RIGHT) {
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
    if (clickType == ClickType.LEFT) {
      if (otherStack.isOf(Items.HEART_OF_THE_SEA)) {
        if (incrementLevel(stack)) {
          otherStack.decrement(1);
          UPGRADE_PARTICLES.accept(player);
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
      int x = (int) (client.mouse.getX()
          * (double) client.getWindow().getScaledWidth()
          / (double) client.getWindow().getWidth());
      int y = (int) (client.mouse.getY()
          * (double) client.getWindow().getScaledHeight()
          / (double) client.getWindow().getHeight());
      ScreenParticleHelper.addScreenParticles(ParticleTypes.END_ROD, x, y, 0.5, 0.5, 0.07, 7);
    }
  }

  @Environment(EnvType.CLIENT)
  private static void itemParticles(ItemStack stack, PlayerEntity player) {
    if (player.world.isClient()) {
      var client = MinecraftClient.getInstance();
      int x = (int) (client.mouse.getX()
          * (double) client.getWindow().getScaledWidth()
          / (double) client.getWindow().getWidth());
      int y = (int) (client.mouse.getY()
          * (double) client.getWindow().getScaledHeight()
          / (double) client.getWindow().getHeight());
      ScreenParticleHelper.addScreenParticles(
          new ItemStackParticleEffect(ParticleTypes.ITEM, stack), x, y, 0.5, 0.5, 0.1, 7);
    }
  }

  @Override
  public void inventoryTick(
      ItemStack stack, World world, Entity entity, int slot, boolean selected) {
    if (!world.isClient()) {
      if (entity instanceof LivingEntity pe) { // selected doesn't account for offhand
        if (!ItemStack.areItemsEqual(stack, pe.getStackInHand(Hand.MAIN_HAND))
            && !ItemStack.areItemsEqual(stack, pe.getStackInHand(Hand.OFF_HAND))) return;
      } else if (!selected) return;

      Set<Item> magnetables = magnetable(stack);
      int level = getLevel(stack);
      world
          .getEntitiesByClass(
              ItemEntity.class,
              new Box(entity.getBlockPos())
                  .expand(level
                      * world
                          .am$get(Magnet.CONFIG)
                          .rangeMultiplier
                          .asDouble(
                              LootContextUtil.fishing(world, entity.getPos(), stack, entity))),
              ie ->
                  magnetables.contains(ie.getDataTracker().get(ItemEntity.STACK).getItem()))
          .forEach(ie -> {
            Vec3d vel = ie.getPos().relativize(entity.getPos()).normalize().multiply(0.05f * level);
            ie.addVelocity(vel.x, vel.y, vel.z);
          });
    }
  }

  @Override
  public Optional<TooltipData> getTooltipData(ItemStack stack) {
    return Optional.of(new BundleTooltipData(new BundleContentsComponent(
        magnetable(stack).stream().map(Item::getDefaultStack).toList())));
  }

  @Override
  public void appendTooltip(
      ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
    tooltip.add(TextUtil.translatable("tooltip.andromeda.magnet.level", getLevel(stack))
        .formatted(Formatting.GRAY));
  }

  private static boolean incrementLevel(ItemStack stack) {
    int level = getLevel(stack);
    if (level >= 5) return false;
    stack.apply(
        COMPONENT_TYPE.get(),
        MagnetContents.DEFAULT,
        contents -> contents.withLevel(contents.level() + 1));
    return true;
  }

  private static int getLevel(ItemStack stack) {
    return stack.getOrDefault(COMPONENT_TYPE.get(), MagnetContents.DEFAULT).level();
  }

  public static void addFirst(ItemStack bundle, ItemStack other) {
    bundle.apply(
        COMPONENT_TYPE.get(),
        MagnetContents.DEFAULT,
        component -> component.withItems(
            Stream.concat(Stream.of(other.getItem()), component.items().stream())
                .collect(ImmutableList.toImmutableList())));
  }

  private static void removeFirst(ItemStack stack) {
    stack.apply(
        COMPONENT_TYPE.get(),
        MagnetContents.DEFAULT,
        component -> component.withItems(
            component.items().stream().skip(1).collect(ImmutableList.toImmutableList())));
  }

  private static Set<Item> magnetable(ItemStack stack) {
    return new LinkedHashSet<>(
        stack.getOrDefault(COMPONENT_TYPE.get(), MagnetContents.DEFAULT).items());
  }

  private void playUpgradeSound(Entity entity) {
    entity.playSound(
        SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
        0.8F,
        0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
  }

  private void playRemoveOneSound(Entity entity) {
    entity.playSound(
        SoundEvents.ITEM_BUNDLE_REMOVE_ONE,
        0.8F,
        0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
  }

  private void playInsertSound(Entity entity) {
    entity.playSound(
        SoundEvents.ITEM_BUNDLE_INSERT,
        0.8F,
        0.8F + entity.getWorld().getRandom().nextFloat() * 0.4F);
  }

  static void init(Magnet module) {
    COMPONENT_TYPE.init(RegistryUtil.register(
        Registries.DATA_COMPONENT_TYPE,
        id("magnet_contents"),
        () -> ComponentType.<MagnetContents>builder()
            .codec(MagnetContents.CODEC)
            .packetCodec(MagnetContents.PACKET_CODEC)
            .build()));
    MagnetItem.MAGNET.init(RegistryUtil.register(
        Registries.ITEM, id("magnet"), () -> new MagnetItem(new Item.Settings().maxCount(1))));

    AndromedaItemGroup.accept(a -> a.keeper(module, ItemGroups.TOOLS, MagnetItem.MAGNET));
  }

  @With
  public record MagnetContents(ImmutableList<Item> items, int level) {

    private MagnetContents(List<Item> items, int level) {
      this(ImmutableList.copyOf(items), level);
    }

    public static final MagnetContents DEFAULT = new MagnetContents(ImmutableList.of(), 1);

    public static final Codec<MagnetContents> CODEC = RecordCodecBuilder.create(data -> data.group(
            Registries.ITEM.getCodec().listOf().fieldOf("items").forGetter(MagnetContents::items),
            Codec.intRange(0, 5).fieldOf("level").forGetter(MagnetContents::level))
        .apply(data, MagnetContents::new));

    public static final PacketCodec<RegistryByteBuf, MagnetContents> PACKET_CODEC =
        PacketCodec.tuple(
            PacketCodecs.registryCodec(Registries.ITEM.getCodec().listOf()),
            MagnetContents::items,
            PacketCodecs.VAR_INT,
            MagnetContents::level,
            MagnetContents::new);
  }
}

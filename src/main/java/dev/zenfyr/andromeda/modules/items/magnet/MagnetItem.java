package dev.zenfyr.andromeda.modules.items.magnet;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.AndromedaItemGroup;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.pulsar.api.client.particles.ScreenParticles;
import dev.zenfyr.pulsar.api.client.particles.VanillaParticles;
import dev.zenfyr.pulsar.api.platform.CEnvType;
import dev.zenfyr.pulsar.api.platform.SupportUtil;
import dev.zenfyr.pulsar.api.util.TextUtil;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import lombok.With;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MagnetItem extends Item {

  public static final ResourceKey<Item> MAGNET_KEY = Andromeda.key(Registries.ITEM, "magnet");
  public static final Keeper<MagnetItem> MAGNET = Keeper.create();
  public static final Keeper<DataComponentType<MagnetContents>> COMPONENT_TYPE = Keeper.create();
  private static final BiConsumer<ItemStack, Player> ITEM_PARTICLES = SupportUtil.support(
      CEnvType.CLIENT, () -> MagnetItem::itemParticles, () -> (stack, player) -> {});
  private static final Consumer<Player> UPGRADE_PARTICLES =
      SupportUtil.support(CEnvType.CLIENT, () -> MagnetItem::upgradeParticles, () -> stack -> {});

  public MagnetItem(Properties settings) {
    super(settings);
  }

  @Override
  public boolean overrideStackedOnOther(
      ItemStack stack, Slot slot, ClickAction clickType, Player player) {
    if (clickType == ClickAction.SECONDARY) {
      ItemStack itemStack = slot.getItem();
      if (itemStack.isEmpty()) {
        removeFirst(stack);
        this.playRemoveOneSound(player);
      } else {
        if (addFirst(stack, itemStack)) {
          ITEM_PARTICLES.accept(itemStack, player);
          this.playInsertSound(player);
        }
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
        if (addFirst(stack, otherStack)) {
          ITEM_PARTICLES.accept(otherStack, player);
          this.playInsertSound(player);
        }
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
      ScreenParticles.get(client)
          .addParticles(
              client.gui.screen(),
              VanillaParticles.create(ParticleTypes.END_ROD, x, y, 0.5, 0.5, 0.07, 7));
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
      ScreenParticles.get(client)
          .addParticles(
              client.gui.screen(),
              VanillaParticles.create(
                  new ItemParticleOption(
                      ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stack)),
                  x,
                  y,
                  0.5,
                  0.5,
                  0.1,
                  7));
    }
  }

  @Override
  public void inventoryTick(
      ItemStack stack, ServerLevel serverLevel, Entity entity, @Nullable EquipmentSlot slot) {
    if (slot != null && slot.getType() == EquipmentSlot.Type.HAND) {
      Set<Item> magnetables = magnetable(stack);
      int level = getLevel(stack);
      serverLevel
          .getEntitiesOfClass(
              ItemEntity.class,
              new AABB(entity.blockPosition())
                  .inflate(level * serverLevel.am$get(Magnet.CONFIG).rangeMultiplier),
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
    return Optional.of(new BundleTooltip(new BundleContents(
        magnetable(stack).stream().map(ItemStackTemplate::new).toList())));
  }

  @Override
  public void appendHoverText(
      ItemStack stack,
      TooltipContext tooltipContext,
      TooltipDisplay tooltipDisplay,
      Consumer<Component> consumer,
      TooltipFlag tooltipFlag) {
    consumer.accept(TextUtil.translatable("tooltip.andromeda.magnet.level", getLevel(stack))
        .withStyle(ChatFormatting.GRAY));
  }

  private static boolean incrementLevel(ItemStack stack) {
    int level = getLevel(stack);
    if (level >= 5) return false;
    stack.update(
        COMPONENT_TYPE.get(),
        MagnetContents.DEFAULT,
        contents -> contents.withLevel(contents.level() + 1));
    return true;
  }

  private static int getLevel(ItemInstance stack) {
    return stack.getOrDefault(COMPONENT_TYPE.get(), MagnetContents.DEFAULT).level();
  }

  public static boolean addFirst(ItemStack bundle, ItemStack other) {
    var contents = bundle.getOrDefault(COMPONENT_TYPE.get(), MagnetContents.DEFAULT);

    if (!contents.items().contains(other.getItem())) {
      bundle.set(
          COMPONENT_TYPE.get(),
          contents.withItems(Stream.concat(Stream.of(other.getItem()), contents.items().stream())
              .collect(ImmutableList.toImmutableList())));
      return true;
    }
    return false;
  }

  private static void removeFirst(ItemStack stack) {
    stack.update(
        COMPONENT_TYPE.get(),
        MagnetContents.DEFAULT,
        component -> component.withItems(
            component.items().stream().skip(1).collect(ImmutableList.toImmutableList())));
  }

  private static Set<Item> magnetable(ItemInstance stack) {
    return new LinkedHashSet<>(
        stack.getOrDefault(COMPONENT_TYPE.get(), MagnetContents.DEFAULT).items());
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

    COMPONENT_TYPE.init(Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        id("magnet_contents"),
        DataComponentType.<MagnetContents>builder()
            .persistent(MagnetContents.CODEC)
            .networkSynchronized(MagnetContents.PACKET_CODEC)
            .build()));

    MagnetItem.MAGNET.init(Registry.register(
        BuiltInRegistries.ITEM,
        MAGNET_KEY,
        new MagnetItem(new Item.Properties().setId(MAGNET_KEY).stacksTo(1))));

    AndromedaItemGroup.BUS.listen(acceptor ->
        acceptor.keeper(module, CreativeModeTabs.TOOLS_AND_UTILITIES, MagnetItem.MAGNET));
  }

  @With
  public record MagnetContents(ImmutableList<Item> items, int level) {

    private MagnetContents(List<Item> items, int level) {
      this(ImmutableList.copyOf(items), level);
    }

    public static final MagnetContents DEFAULT = new MagnetContents(ImmutableList.of(), 1);

    public static final Codec<MagnetContents> CODEC = RecordCodecBuilder.create(data -> data.group(
            BuiltInRegistries.ITEM
                .byNameCodec()
                .listOf()
                .fieldOf("items")
                .forGetter(MagnetContents::items),
            Codec.intRange(0, 5).fieldOf("level").forGetter(MagnetContents::level))
        .apply(data, MagnetContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MagnetContents> PACKET_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistries(
                BuiltInRegistries.ITEM.byNameCodec().listOf()),
            MagnetContents::items,
            ByteBufCodecs.VAR_INT,
            MagnetContents::level,
            MagnetContents::new);
  }
}

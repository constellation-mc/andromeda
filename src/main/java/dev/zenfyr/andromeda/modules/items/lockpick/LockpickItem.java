package dev.zenfyr.andromeda.modules.items.lockpick;

import static dev.zenfyr.andromeda.common.Andromeda.id;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.common.Andromeda;
import dev.zenfyr.andromeda.common.util.AndromedaCreativeTab;
import dev.zenfyr.andromeda.common.util.Keeper;
import dev.zenfyr.andromeda.modules.blocks.guarded_loot.GuardedLoot;
import dev.zenfyr.andromeda.modules.blocks.guarded_loot.GuardedLootMain;
import dev.zenfyr.pulsar.api.util.MathUtil;
import dev.zenfyr.pulsar.api.util.TextUtil;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LockpickItem extends Item {

  public static final ResourceKey<Item> LOCKPICK_KEY = Andromeda.key(Registries.ITEM, "lockpick");
  public static final Keeper<LockpickItem> INSTANCE = Keeper.create();

  public LockpickItem(Properties settings) {
    super(settings);
  }

  public boolean tryUse(ItemStack stack, LivingEntity user, InteractionHand hand) {
    var c = user.level().am$get(Lockpick.CONFIG);
    if (c.available && hand == InteractionHand.MAIN_HAND) {
      if (!(user instanceof Player p && p.getAbilities().instabuild)) {
        if (c.breakAfterUse) {
          if (!user.level().isClientSide())
            user.onEquippedItemBroken(LockpickItem.INSTANCE.orThrow(), EquipmentSlot.MAINHAND);

          stack.shrink(1);
        }
      }

      return c.chance - 1 == 0 || MathUtil.threadRandom().nextInt(c.chance - 1) == 0;
    }
    return false;
  }

  @Override
  public InteractionResult interactLivingEntity(
      ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
    if (user.level().isClientSide()) return InteractionResult.SUCCESS;

    if (entity instanceof AbstractVillager merchant
        && Andromeda.MAIN.get(Lockpick.MAIN_CONFIG).villagerInventory) {
      if (tryUse(stack, user, hand)) {
        user.openMenu(new SimpleMenuProvider(
            (syncId, inv, player) ->
                new MerchantInventoryScreenHandler(syncId, inv, merchant.getInventory()),
            TextUtil.translatable("gui.andromeda.merchant")));
        return InteractionResult.SUCCESS;
      }
    }
    return InteractionResult.CONSUME;
  }

  static void init() {
    var module = ModuleManager.get().get(Lockpick.class).orElseThrow();
    var config = Andromeda.MAIN.get(Lockpick.MAIN_CONFIG);

    LockpickItem.INSTANCE.init(Registry.register(
        BuiltInRegistries.ITEM,
        LOCKPICK_KEY,
        new LockpickItem(new Item.Properties().setId(LOCKPICK_KEY).stacksTo(16))));

    if (config.villagerInventory) {
      MerchantInventoryScreenHandler.INSTANCE.init(Registry.register(
          BuiltInRegistries.MENU,
          id("merchant_inventory"),
          new MenuType<>(MerchantInventoryScreenHandler::new, FeatureFlagSet.of())));
    }

    AndromedaCreativeTab.BUS.listen(acceptor ->
        acceptor.keeper(module, CreativeModeTabs.TOOLS_AND_UTILITIES, LockpickItem.INSTANCE));

    ModuleManager.get().get(GuardedLoot.class).ifPresent(gl -> {
      GuardedLootMain.UNLOCKERS.add((blockEntity, player) -> {
        if (player.level().am$get(GuardedLoot.CONFIG).allowLockPicking) {
          if (player.getMainHandItem().is(LockpickItem.INSTANCE.orThrow())) {
            return LockpickItem.INSTANCE
                .orThrow()
                .tryUse(player.getMainHandItem(), player, InteractionHand.MAIN_HAND);
          }
        }
        return false;
      });
    });
  }
}

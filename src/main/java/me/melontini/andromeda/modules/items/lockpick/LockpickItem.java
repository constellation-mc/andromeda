package me.melontini.andromeda.modules.items.lockpick;

import static me.melontini.andromeda.common.Andromeda.id;

import me.melontini.andromeda.api.ModuleDeclarations;
import me.melontini.andromeda.bootstrap.ModuleManager;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.util.AndromedaItemGroup;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.andromeda.modules.blocks.guarded_loot.GuardedLoot;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LockpickItem extends Item {

  public static final Keeper<LockpickItem> INSTANCE = Keeper.create();

  public LockpickItem(Properties settings) {
    super(settings);
  }

  public boolean tryUse(ItemStack stack, LivingEntity user, InteractionHand hand) {
    var c = user.level.am$get(Lockpick.CONFIG);
    var supplier = LootContextBuilder.fishing(
        user.level, builder -> builder.origin(user).tool(user, hand).thisEntity(user));
    if (c.available.asBoolean(supplier) && hand == InteractionHand.MAIN_HAND) {
      int chance = c.chance.asInt(supplier);

      if (!(user instanceof Player p && p.getAbilities().instabuild)) {
        if (c.breakAfterUse.asBoolean(supplier)) {
          if (!user.level.isClientSide()) user.broadcastBreakEvent(EquipmentSlot.MAINHAND);

          stack.shrink(1);
        }
      }

      return chance - 1 == 0 || MathUtil.threadRandom().nextInt(chance - 1) == 0;
    }
    return false;
  }

  @Override
  public InteractionResult interactLivingEntity(
      ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
    if (user.level.isClientSide()) return InteractionResult.SUCCESS;

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

    LockpickItem.INSTANCE.init(RegistryUtil.register(
        BuiltInRegistries.ITEM,
        id("lockpick"),
        () -> new LockpickItem(new FabricItemSettings().stacksTo(16))));
    MerchantInventoryScreenHandler.INSTANCE.init(RegistryUtil.register(
        config.villagerInventory,
        BuiltInRegistries.MENU,
        id("merchant_inventory"),
        RegistryUtil.screenHandlerType(MerchantInventoryScreenHandler::new)));

    AndromedaItemGroup.BUS.listen(acceptor ->
        acceptor.keeper(module, CreativeModeTabs.TOOLS_AND_UTILITIES, LockpickItem.INSTANCE));

    ModuleManager.get()
        .whenAvailable(
            ModuleDeclarations.LOOT_UNLOCKER,
            function -> function.apply((be, player) -> {
              if (player.level.am$get(GuardedLoot.CONFIG).allowLockPicking) {
                if (player.getMainHandItem().is(LockpickItem.INSTANCE.orThrow())) {
                  return LockpickItem.INSTANCE
                      .orThrow()
                      .tryUse(player.getMainHandItem(), player, InteractionHand.MAIN_HAND);
                }
              }
              return false;
            }));
  }
}

package me.melontini.andromeda.modules.items.lockpick;

import static me.melontini.andromeda.common.Andromeda.id;

import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.AndromedaItemGroup;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.base.util.functions.Memoize;
import me.melontini.dark_matter.api.minecraft.util.RegistryUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class LockpickItem extends Item {

  public static final Keeper<LockpickItem> INSTANCE = Keeper.create();

  public LockpickItem(Settings settings) {
    super(settings);
  }

  public boolean tryUse(ItemStack stack, LivingEntity user, Hand hand) {
    var c = user.world.am$get(Lockpick.CONFIG);
    var supplier = Memoize.supplier(
        LootContextUtil.fishing(user.world, user.getPos(), user.getStackInHand(hand), user));
    if (c.available.asBoolean(supplier) && hand == Hand.MAIN_HAND) {
      int chance = c.chance.asInt(supplier);

      if (!(user instanceof PlayerEntity p && p.getAbilities().creativeMode)) {
        if (c.breakAfterUse.asBoolean(supplier)) {
          if (!user.world.isClient()) user.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);

          stack.decrement(1);
        }
      }

      return chance - 1 == 0 || MathUtil.threadRandom().nextInt(chance - 1) == 0;
    }
    return false;
  }

  @Override
  public ActionResult useOnEntity(
      ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
    if (user.world.isClient()) return ActionResult.SUCCESS;

    if (entity instanceof MerchantEntity merchant
        && Andromeda.ROOT_HANDLER.get(Lockpick.MAIN_CONFIG).villagerInventory) {
      if (tryUse(stack, user, hand)) {
        user.openHandledScreen(new SimpleNamedScreenHandlerFactory(
            (syncId, inv, player) ->
                new MerchantInventoryScreenHandler(syncId, inv, merchant.getInventory()),
            TextUtil.translatable("gui.andromeda.merchant")));
        return ActionResult.SUCCESS;
      }
    }
    return ActionResult.CONSUME;
  }

  static void init(Lockpick module, Lockpick.MainConfig config) {
    LockpickItem.INSTANCE.init(RegistryUtil.register(
        Registries.ITEM,
        id("lockpick"),
        () -> new LockpickItem(new FabricItemSettings().maxCount(16))));
    MerchantInventoryScreenHandler.INSTANCE.init(RegistryUtil.register(
        config.villagerInventory,
        Registries.SCREEN_HANDLER,
        id("merchant_inventory"),
        RegistryUtil.screenHandlerType(MerchantInventoryScreenHandler::new)));

    AndromedaItemGroup.accept(
        acceptor -> acceptor.keeper(module, ItemGroups.TOOLS, LockpickItem.INSTANCE));
  }
}

package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class LockpickItem extends Item {

    public LockpickItem(Settings settings) {
        super(settings);
    }

    public boolean tryUse(Lockpick module, ItemStack stack, LivingEntity user, Hand hand) {
        if (module.enabled()) {

            if (!(user instanceof PlayerEntity p && p.getAbilities().creativeMode)) {
                if (module.config().breakAfterUse) {
                    if (!user.world.isClient())
                        user.sendEquipmentBreakStatus(hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);

                    stack.decrement(1);
                }
            }

            return module.rollLockpick();
        }
        return false;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        Lockpick module = ModuleManager.quick(Lockpick.class);
        if (entity instanceof MerchantEntity merchant && module.config().villagerInventory) {
            if (tryUse(module, stack, user, hand)) {
                if (user.world.isClient()) return ActionResult.SUCCESS;

                user.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> new MerchantInventoryScreenHandler(syncId, inv, merchant.getInventory()), TextUtil.translatable("gui.andromeda.merchant")));
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.CONSUME;
    }
}

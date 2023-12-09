package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
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

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        Lockpick module = ModuleManager.quick(Lockpick.class);
        if (module.config().enabled && module.rollLockpick()) {
            if (entity instanceof MerchantEntity merchant && module.config().villagerInventory) {
                if (user.world.isClient()) return ActionResult.SUCCESS;

                user.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> new MerchantInventoryScreenHandler(syncId, inv, merchant.getInventory()), TextUtil.translatable("gui.andromeda.merchant")));
                if (!user.getAbilities().creativeMode && module.config().breakAfterUse) stack.decrement(1);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.CONSUME;
    }
}

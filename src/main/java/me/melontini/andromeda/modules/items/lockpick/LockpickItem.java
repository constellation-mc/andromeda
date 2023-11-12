package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.config.Config;
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
        if (Config.get().lockpick.enable && (Config.get().lockpick.chance - 1 == 0 || user.world.random.nextInt(Config.get().lockpick.chance - 1) == 0)) {
            if (entity instanceof MerchantEntity merchant && Config.get().lockpick.villagerInventory) {
                if (user.world.isClient()) return ActionResult.SUCCESS;

                user.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> new MerchantInventoryScreenHandler(syncId, inv, merchant.getInventory()), TextUtil.translatable("gui.andromeda.merchant")));
                if (!user.getAbilities().creativeMode && Config.get().lockpick.breakAfterUse) stack.decrement(1);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.CONSUME;
    }
}

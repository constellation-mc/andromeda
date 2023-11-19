package me.melontini.andromeda.modules.items.lockpick;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.dark_matter.api.base.util.classes.Lazy;
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
    private static final Lazy<Lockpick> module = Lazy.of(() -> () -> ModuleManager.quick(Lockpick.class));
    public LockpickItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (module.get().config().enabled && (module.get().config().chance - 1 == 0 || user.world.random.nextInt(module.get().config().chance - 1) == 0)) {
            if (entity instanceof MerchantEntity merchant && module.get().config().villagerInventory) {
                if (user.world.isClient()) return ActionResult.SUCCESS;

                user.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> new MerchantInventoryScreenHandler(syncId, inv, merchant.getInventory()), TextUtil.translatable("gui.andromeda.merchant")));
                if (!user.getAbilities().creativeMode && module.get().config().breakAfterUse) stack.decrement(1);
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.CONSUME;
    }
}

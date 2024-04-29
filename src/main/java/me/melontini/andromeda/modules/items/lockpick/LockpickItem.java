package me.melontini.andromeda.modules.items.lockpick;

import com.google.common.base.Suppliers;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.util.Keeper;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class LockpickItem extends Item {

    public static final Keeper<LockpickItem> INSTANCE = Keeper.create();

    public LockpickItem(Settings settings) {
        super(settings);
    }

    public boolean tryUse(Lockpick module, ItemStack stack, LivingEntity user, Hand hand) {
        if (Andromeda.getConfig(module).e.enabled() && hand == Hand.MAIN_HAND) {
            var c = Andromeda.getConfig(module).c;
            var supplier = Suppliers.memoize(() -> {
                LootContextParameterSet set = new LootContextParameterSet.Builder((ServerWorld) user.world)
                        .add(LootContextParameters.ORIGIN, user.getPos())
                        .add(LootContextParameters.TOOL, stack)
                        .add(LootContextParameters.THIS_ENTITY, user)
                        .build(LootContextTypes.FISHING);

                return new LootContext.Builder(set).build(null);
            });
            int chance = c.chance.asInt(supplier);

            if (!(user instanceof PlayerEntity p && p.getAbilities().creativeMode)) {
                if (Andromeda.getConfig(module).c.breakAfterUse.asBoolean(supplier)) {
                    if (!user.world.isClient())
                        user.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);

                    stack.decrement(1);
                }
            }


            return chance - 1 == 0 || MathUtil.threadRandom().nextInt(chance - 1) == 0;
        }
        return false;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (user.world.isClient()) return ActionResult.SUCCESS;

        Lockpick module = ModuleManager.quick(Lockpick.class);
        if (entity instanceof MerchantEntity merchant && Andromeda.getConfig(module).c.villagerInventory) {
            if (tryUse(module, stack, user, hand)) {
                user.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inv, player) -> new MerchantInventoryScreenHandler(syncId, inv, merchant.getInventory()), TextUtil.translatable("gui.andromeda.merchant")));
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.CONSUME;
    }
}

package me.melontini.andromeda.modules.mechanics.villager_gifting.mixin;

import me.melontini.andromeda.modules.mechanics.villager_gifting.GiftTags;
import me.melontini.andromeda.modules.mechanics.villager_gifting.VillagerGifting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.VillageGossipType;
import net.minecraft.village.VillagerGossips;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(VillagerEntity.class)
abstract class VillagerEntityMixin extends MerchantEntity {

    @Shadow @Final private VillagerGossips gossip;
    @Shadow protected abstract void sayNo();

    public VillagerEntityMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/VillagerEntity;getOffers()Lnet/minecraft/village/TradeOfferList;", shift = At.Shift.BEFORE), cancellable = true, method = "interactMob")
    private void andromeda$useGifts(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (hand != Hand.MAIN_HAND || world.isClient()) return;
        if (!world.am$get(VillagerGifting.class).enabled) return;

        ItemStack stack = player.getStackInHand(hand);

        ItemStack gift = stack.copy();
        gift.setCount(1);

        for (Map.Entry<TagKey<Item>, GiftTags.Action> entry : GiftTags.ACTION_MAP.entrySet()) {
            if (stack.isIn(entry.getKey())) {
                if (andromeda$tryInsertGift(cir, player, gift, entry.getValue().type())) {
                    this.world.sendEntityStatus(this, entry.getValue().status());
                    if (!player.isCreative()) stack.decrement(1);
                    break;
                }
            }
        }
    }

    @Unique
    private boolean andromeda$tryInsertGift(CallbackInfoReturnable<ActionResult> cir, PlayerEntity player, ItemStack stack, VillageGossipType type) {
        if (this.getInventory().canInsert(stack)) {
            this.getInventory().addStack(stack);
            this.gossip.startGossip(player.getUuid(), type, 3);
            cir.setReturnValue(ActionResult.success(this.world.isClient));
            return true;
        } else {
            this.sayNo();
            cir.setReturnValue(ActionResult.success(this.world.isClient));
            return false;
        }
    }
}

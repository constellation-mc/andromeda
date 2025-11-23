package me.melontini.andromeda.modules.mechanics.villager_gifting.mixin;

import java.util.Map;
import me.melontini.andromeda.common.util.LootContextBuilder;
import me.melontini.andromeda.modules.mechanics.villager_gifting.GiftTags;
import me.melontini.andromeda.modules.mechanics.villager_gifting.VillagerGifting;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
abstract class VillagerEntityMixin extends AbstractVillager {

  @Shadow
  @Final
  private GossipContainer gossips;

  @Shadow
  protected abstract void setUnhappy();

  public VillagerEntityMixin(EntityType<? extends AbstractVillager> entityType, Level world) {
    super(entityType, world);
  }

  @Inject(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/entity/npc/Villager;getOffers()Lnet/minecraft/world/item/trading/MerchantOffers;",
              shift = At.Shift.BEFORE),
      cancellable = true,
      method = "mobInteract")
  private void andromeda$useGifts(
      Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
    if (hand != InteractionHand.MAIN_HAND || level.isClientSide()) return;
    ItemStack stack = player.getItemInHand(hand);

    if (!level
        .am$get(VillagerGifting.CONFIG)
        .available
        .asBoolean(LootContextBuilder.fishing(
            level, builder -> builder.origin(player).tool(stack).thisEntity(player)))) return;

    ItemStack gift = stack.copy();
    gift.setCount(1);

    for (Map.Entry<TagKey<Item>, GiftTags.Action> entry : GiftTags.ACTION_MAP.entrySet()) {
      if (stack.is(entry.getKey())) {
        if (andromeda$tryInsertGift(cir, player, gift, entry.getValue().type())) {
          this.level.broadcastEntityEvent(this, entry.getValue().status());
          if (!player.isCreative()) stack.shrink(1);
          break;
        }
      }
    }
  }

  @Unique private boolean andromeda$tryInsertGift(
      CallbackInfoReturnable<InteractionResult> cir,
      Player player,
      ItemStack stack,
      GossipType type) {
    if (this.getInventory().canAddItem(stack)) {
      this.getInventory().addItem(stack);
      this.gossips.add(player.getUUID(), type, 3);
      cir.setReturnValue(InteractionResult.sidedSuccess(this.level.isClientSide));
      return true;
    } else {
      this.setUnhappy();
      cir.setReturnValue(InteractionResult.sidedSuccess(this.level.isClientSide));
      return false;
    }
  }
}

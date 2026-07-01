package dev.zenfyr.andromeda.modules.items.pouches;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;

public class Trades {

  public static void register() {
    if (PouchesMain.SAPLING_POUCH.isPresent()) {
      TradeOfferHelper.registerVillagerOffers(
          VillagerProfession.FARMER,
          2,
          factories -> factories.add((entity, random) -> new MerchantOffer(
              new ItemStack(Items.EMERALD, 5),
              new ItemStack(PouchesMain.SAPLING_POUCH.orThrow(), 1),
              12,
              4,
              0.06f)));
    }

    if (PouchesMain.FLOWER_POUCH.isPresent()) {
      TradeOfferHelper.registerVillagerOffers(
          VillagerProfession.FARMER,
          2,
          factories -> factories.add((entity, random) -> new MerchantOffer(
              new ItemStack(Items.EMERALD, 4),
              new ItemStack(PouchesMain.FLOWER_POUCH.orThrow(), 1),
              12,
              4,
              0.06f)));
    }

    if (PouchesMain.SEED_POUCH.isPresent()) {
      TradeOfferHelper.registerVillagerOffers(
          VillagerProfession.FARMER,
          2,
          factories -> factories.add((entity, random) -> new MerchantOffer(
              new ItemStack(Items.EMERALD, 3),
              new ItemStack(PouchesMain.SEED_POUCH.orThrow(), 1),
              12,
              4,
              0.06f)));
    }
  }
}

package me.melontini.andromeda.modules.items.pouches;

import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;

public class Trades {

  public static void register() {
    if (Main.SAPLING_POUCH.isPresent()) {
      TradeOfferHelper.registerVillagerOffers(
          VillagerProfession.FARMER,
          2,
          factories -> factories.add((entity, random) -> new TradeOffer(
              new ItemStack(Items.EMERALD, 5),
              new ItemStack(Main.SAPLING_POUCH.orThrow(), 1),
              12,
              4,
              0.06f)));
    }

    if (Main.FLOWER_POUCH.isPresent()) {
      TradeOfferHelper.registerVillagerOffers(
          VillagerProfession.FARMER,
          2,
          factories -> factories.add((entity, random) -> new TradeOffer(
              new ItemStack(Items.EMERALD, 4),
              new ItemStack(Main.FLOWER_POUCH.orThrow(), 1),
              12,
              4,
              0.06f)));
    }

    if (Main.SEED_POUCH.isPresent()) {
      TradeOfferHelper.registerVillagerOffers(
          VillagerProfession.FARMER,
          2,
          factories -> factories.add((entity, random) -> new TradeOffer(
              new ItemStack(Items.EMERALD, 3),
              new ItemStack(Main.SEED_POUCH.orThrow(), 1),
              12,
              4,
              0.06f)));
    }
  }
}

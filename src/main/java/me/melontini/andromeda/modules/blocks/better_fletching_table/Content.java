package me.melontini.andromeda.modules.blocks.better_fletching_table;

import com.google.common.collect.Sets;
import me.melontini.andromeda.base.Bootstrap;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

import java.util.Set;

import static me.melontini.andromeda.common.registries.Common.id;

public class Content {

    public static final Keeper<ScreenHandlerType<FletchingScreenHandler>> FLETCHING = Keeper.of(() -> () ->
            RegistryUtil.createScreenHandler(id("fletching"), () -> FletchingScreenHandler::new));

    public static void init(BetterFletchingTable module) {
        Set<Item> tightable = Sets.newHashSet(Items.BOW, Items.CROSSBOW);

        if (Bootstrap.isModLoaded(module, "additionaladditions")) {
            CommonRegistries.items().getOrEmpty(Identifier.tryParse("additionaladditions:crossbow_with_spyglass"))
                    .ifPresent(item -> {
                        tightable.add(item);
                        FletchingScreenHandler.addRecipe(stack -> {
                                    var result = new ItemStack(item, 1);
                                    if (stack.getNbt() != null) result.setNbt(stack.getNbt());
                                    return result;
                                },
                                Ingredient.ofItems(Items.SPYGLASS), Ingredient.ofItems(Items.CROSSBOW));
                    });
        }

        FletchingScreenHandler.addRecipe(stack -> {
            NbtCompound nbt = stack.getOrCreateNbt();
            int i = nbt.getInt("AM-Tightened");
            if (i >= 32) return ItemStack.EMPTY;

            ItemStack newStack = stack.copy();
            newStack.getOrCreateNbt().putInt("AM-Tightened", Math.min(i + 2, 32));
            return newStack;
        }, Ingredient.ofItems(Items.STRING), Ingredient.ofItems(tightable.toArray(ItemConvertible[]::new)));
    }
}

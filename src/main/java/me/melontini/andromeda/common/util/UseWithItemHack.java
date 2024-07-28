package me.melontini.andromeda.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class UseWithItemHack {
    private static final ThreadLocal<Context> LOCAL = ThreadLocal.withInitial(() -> null);

    public static void setContext(Context context) {
        LOCAL.set(context);
    }

    public static Context getContext() {
        return LOCAL.get();
    }

    public record Context(ItemStack stack, Hand hand) { }
}

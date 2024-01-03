package me.melontini.andromeda.modules.gui.gui_particles.mixin;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.gui.gui_particles.GuiParticles;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BundleItem.class)
abstract class BundleItemMixin {

    @Inject(at = @At("RETURN"), method = "addToBundle")
    private static void andromeda$spawnParticles(ItemStack bundle, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValueI() > 0 && ModuleManager.quick(GuiParticles.class).config().bundleInputParticles) {
            Support.run(EnvType.CLIENT, () -> () -> {
                var client = MinecraftClient.getInstance();
                if (client.isOnThread() && client.currentScreen != null) {
                    int x = (int) (client.mouse.getX() * (double) client.getWindow().getScaledWidth() / (double) client.getWindow().getWidth());
                    int y = (int) (client.mouse.getY() * (double) client.getWindow().getScaledHeight() / (double) client.getWindow().getHeight());
                    ScreenParticleHelper.addScreenParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, stack),
                            x, y, 0.5, 0.5, 0.1, 7);
                }
            });
        }
    }
}

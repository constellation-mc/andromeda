package me.melontini.andromeda.mixin.gui.gui_particles;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.gui.gui_particles.GuiParticles;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentScreen.class)
abstract class EnchantmentScreenMixin extends HandledScreen<EnchantmentScreenHandler> {
    @Unique
    private static final GuiParticles am$guip = ModuleManager.quick(GuiParticles.class);
    public EnchantmentScreenMixin(EnchantmentScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickButton(II)V", shift = At.Shift.AFTER), method = "mouseClicked")
    private void andromeda$particles(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!am$guip.config().enchantmentScreenParticles) return;

        Slot slot = this.handler.slots.get(0);
        ScreenParticleHelper.addScreenParticles(ParticleTypes.END_ROD, this.x + slot.x + 8, this.y + slot.y + 8, 0.5, 0.5, 0.07, 10);
    }
}

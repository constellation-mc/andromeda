package me.melontini.andromeda.mixin.gui.gui_particles;

import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.client.particles.screen.CustomItemStackParticle;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import me.melontini.dark_matter.api.base.util.MathStuff;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(CreativeInventoryScreen.class)
@MixinRelatedConfigOption("guiParticles.creativeScreenParticles")
public abstract class CreativeInventoryScreenMixin extends AbstractInventoryScreen<CreativeInventoryScreen.CreativeScreenHandler> {
    public CreativeInventoryScreenMixin(CreativeInventoryScreen.CreativeScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;clickCreativeStack(Lnet/minecraft/item/ItemStack;I)V", ordinal = 0, shift = At.Shift.BEFORE), method = "onMouseClick")
    private void andromeda$clickDeleteParticles(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci, @Local(ordinal = 2) int index) {
        if (!Andromeda.CONFIG.guiParticles.creativeScreenParticles) return;

        Slot slot1 = this.handler.slots.get(index);
        ScreenParticleHelper.addScreenParticle(new CustomItemStackParticle(this.x + slot1.x + 8, this.y + slot1.y + 8,
                MathStuff.nextDouble(
                        -Andromeda.CONFIG.guiParticles.creativeScreenParticlesVelX,
                        Andromeda.CONFIG.guiParticles.creativeScreenParticlesVelX), 0.6, slot1.getStack()));
    }
}

package me.melontini.andromeda.mixin.internal;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.Reader;
import java.util.List;
import java.util.Optional;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z", shift = At.Shift.BY, by = 2), method = "loadTextureList", cancellable = true)
    private void andromeda$skipRedundant(Identifier id, Resource resource, CallbackInfoReturnable<Optional<List<Identifier>>> cir, @Local List<Identifier> list, @Local LocalBooleanRef bl, @Local Reader reader) {
        if (list != null && !bl.get()) {
           cir.setReturnValue(Optional.empty());
           if (reader != null)
               Utilities.runUnchecked(reader::close); //f u
        }
    }
}

package me.melontini.andromeda.common.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.Reader;
import java.util.List;
import java.util.Map;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z", shift = At.Shift.BY, by = 2), method = "loadTextureList", cancellable = true)
    private void andromeda$skipRedundant(ResourceManager resourceManager, Identifier id, Map<Identifier, List<Identifier>> result, CallbackInfo ci, @Local List<Identifier> list, @Local LocalBooleanRef bl, @Local Reader reader) {
        if (list != null && !bl.get()) {
           ci.cancel();
           if (reader != null)
               Utilities.runUnchecked(reader::close); //f u
        }
    }
}

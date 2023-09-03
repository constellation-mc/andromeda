package me.melontini.andromeda.mixin.mechanics.dragon_fight;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.MixinRelatedConfigOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net/minecraft/world/gen/feature/EndSpikeFeature$SpikeCache")
@MixinRelatedConfigOption({"dragonFight.fightTweaks", "dragonFight.shorterSpikes"})
public class SpikeCacheMixin {
    @ModifyExpressionValue(method = "load(Ljava/lang/Long;)Ljava/util/List;", at = @At(value = "CONSTANT", args = "intValue=76"))
    private int andromeda$modifySpikeSize(int size) {
        if (Config.get().dragonFight.fightTweaks && Config.get().dragonFight.shorterSpikes) return 72;
        return size;
    }

    @ModifyExpressionValue(method = "load(Ljava/lang/Long;)Ljava/util/List;", at = @At(value = "CONSTANT", args = "intValue=3", ordinal = 1))
    private int andromeda$modifySpikeHeight(int size) {
        if (Config.get().dragonFight.fightTweaks && Config.get().dragonFight.shorterSpikes) return 2;
        return size;
    }
}

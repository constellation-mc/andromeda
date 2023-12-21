package me.melontini.andromeda.common.mixin;

import me.melontini.andromeda.common.config.ScopedConfigs;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
public class WorldMixin implements ScopedConfigs.WorldExtension {
}

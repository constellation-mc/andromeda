package me.melontini.andromeda.common.mixin.configs;

import me.melontini.andromeda.common.config.ScopedConfigs;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(World.class)
abstract class WorldMixin implements ScopedConfigs.WorldExtension {
}

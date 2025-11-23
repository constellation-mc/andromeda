package me.melontini.andromeda.common.mixin.configs;

import me.melontini.andromeda.common.config.DataConfigs;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
abstract class WorldMixin implements DataConfigs.WorldExtension {}

package dev.zenfyr.andromeda.common.mixin.configs;

import dev.zenfyr.andromeda.common.config.DataConfigs;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
abstract class LevelMixin implements DataConfigs.LevelExtension {}

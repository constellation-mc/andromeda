package me.melontini.andromeda.common.mixin.init;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;

@SpecialEnvironment(Environment.CLIENT)
@Mixin(Main.class)
abstract class ClientMainMixin {
}

package dev.zenfyr.andromeda.common.mixin;

import dev.zenfyr.andromeda.bootstrap.util.mixin.MixinEnvironment;
import dev.zenfyr.pulsar.api.platform.CEnvType;
import java.util.List;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@MixinEnvironment(CEnvType.CLIENT)
@Mixin(MultiElementListEntry.class)
public interface MultiElementListEntryAccessor {

  @Accessor("entries")
  List<AbstractConfigListEntry<?>> pulsar$entries();
}

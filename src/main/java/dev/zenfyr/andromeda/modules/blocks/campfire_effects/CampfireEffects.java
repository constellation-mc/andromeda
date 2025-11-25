package dev.zenfyr.andromeda.modules.blocks.campfire_effects;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleInfo;
import dev.zenfyr.andromeda.bootstrap.config.ConfigDefinition;
import dev.zenfyr.andromeda.bootstrap.config.RegisterConfigEvent;
import dev.zenfyr.andromeda.bootstrap.util.Environment;
import dev.zenfyr.andromeda.common.config.GameConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

@ModuleInfo(name = "campfire_effects", category = "blocks", env = Environment.SERVER)
public final class CampfireEffects extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  CampfireEffects() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  public static final class Config extends GameConfig {
    public boolean affectsPassive = true;
    public double effectsRange = 10;
    public List<Effect> effectList = Lists.newArrayList(new Effect());

    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class Effect {
      public MobEffect identifier = MobEffects.REGENERATION;
      public int amplifier = 0;
    }
  }
}

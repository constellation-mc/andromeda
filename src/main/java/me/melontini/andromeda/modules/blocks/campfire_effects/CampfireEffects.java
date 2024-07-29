package me.melontini.andromeda.modules.blocks.campfire_effects;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;
import me.melontini.andromeda.util.commander.number.LongIntermediary;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

@ModuleInfo(name = "campfire_effects", category = "blocks", environment = Environment.SERVER)
public final class CampfireEffects extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  CampfireEffects() {
    this.defineConfig(ConfigState.GAME, CONFIG);
  }

  @ToString
  public static final class Config extends GameConfig {
    public BooleanIntermediary affectsPassive = BooleanIntermediary.of(true);
    public DoubleIntermediary effectsRange = DoubleIntermediary.of(10);
    public List<Effect> effectList = Lists.newArrayList(new Effect());

    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class Effect {
      public StatusEffect identifier = StatusEffects.REGENERATION;
      public LongIntermediary amplifier = LongIntermediary.of(0);
    }
  }
}

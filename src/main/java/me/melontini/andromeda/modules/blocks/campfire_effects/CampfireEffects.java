package me.melontini.andromeda.modules.blocks.campfire_effects;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.melontini.andromeda.bootstrap.Module;
import me.melontini.andromeda.bootstrap.ModuleInfo;
import me.melontini.andromeda.bootstrap.config.ConfigDefinition;
import me.melontini.andromeda.bootstrap.config.RegisterConfigEvent;
import me.melontini.andromeda.bootstrap.util.Environment;
import me.melontini.andromeda.common.config.GameConfig;
import me.melontini.andromeda.common.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.common.util.commander.number.DoubleIntermediary;
import me.melontini.andromeda.common.util.commander.number.LongIntermediary;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

@ModuleInfo(name = "campfire_effects", category = "blocks", env = Environment.SERVER)
public final class CampfireEffects extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  CampfireEffects() {
    RegisterConfigEvent.get(this, RegisterConfigEvent.GAME).listen(() -> CONFIG);
  }

  public static final class Config extends GameConfig {
    public BooleanIntermediary affectsPassive = BooleanIntermediary.of(true);
    public DoubleIntermediary effectsRange = DoubleIntermediary.of(10);
    public List<Effect> effectList = Lists.newArrayList(new Effect());

    @ToString
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class Effect {
      public MobEffect identifier = MobEffects.REGENERATION;
      public LongIntermediary amplifier = LongIntermediary.of(0);
    }
  }
}

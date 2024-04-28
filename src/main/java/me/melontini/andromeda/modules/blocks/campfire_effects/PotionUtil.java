package me.melontini.andromeda.modules.blocks.campfire_effects;

import lombok.SneakyThrows;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.Andromeda;
import me.melontini.andromeda.common.config.ScopedConfigs;
import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.util.ServerHelper;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

public class PotionUtil {

    @SneakyThrows
    public static @NotNull StatusEffect getStatusEffect(World world, Identifier id) {
        StatusEffect effect = CommonRegistries.statusEffects().get(id);
        if (effect == null) {
            CampfireEffects m = ModuleManager.quick(CampfireEffects.class);

            ServerHelper.broadcastToOps(requireNonNull(world.getServer()), TextUtil.literal((
                            "(Andromeda) Couldn't get StatusEffect from identifier '%s'.%nReturning 'regeneration' and resetting config to default!").formatted(id))
                    .formatted(Formatting.RED));

            world.am$get(m).c.effectList = Andromeda.rootHandler().getDefault(m).c.effectList;
            ScopedConfigs.getConfigs((ServerWorld) world).save(m);
            return CommonRegistries.statusEffects().getOrEmpty(Identifier.tryParse("minecraft:regeneration")).orElseThrow();
        }
        return effect;
    }
}

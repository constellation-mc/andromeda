package me.melontini.andromeda.common.mixin;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.common.util.LazyLootParameterSet;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.dark_matter.api.base.util.Utilities;
import net.minecraft.entity.Entity;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
abstract class EntityMixin implements ConstantLootContextAccessor {

    @Shadow public abstract Vec3d getPos();

    @Shadow public abstract World getWorld();

    @Unique private final Supplier<LootContext> andromeda$context = Utilities.supply(() -> {
        var c = new LazyLootParameterSet.Builder(() -> (ServerWorld) getWorld())
                .add(LootContextParameters.ORIGIN, this::getPos)
                .add(LootContextParameters.THIS_ENTITY, () -> (Entity) (Object) this)
                .build(LootContextTypes.COMMAND);
        return Suppliers.memoize(() -> LootContextUtil.build(c));
    });

    @Override
    public Supplier<LootContext> andromeda$getLootContext() {
        return andromeda$context;
    }
}

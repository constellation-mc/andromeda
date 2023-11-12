package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.registries.Keeper;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.content.RegistryUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import static me.melontini.andromeda.registries.Common.id;

public class Content {
    @Feature("throwableItems.enable")
    public static final Keeper<EntityType<FlyingItemEntity>> FLYING_ITEM = Keeper.of(() -> () ->
            RegistryUtil.createEntityType(id("flying_item"),
                    FabricEntityTypeBuilder.<FlyingItemEntity>create(SpawnGroup.MISC, FlyingItemEntity::new)
                            .dimensions(new EntityDimensions(0.25F, 0.25F, true)).trackRangeChunks(4).trackedUpdateRate(10)));

    public static DamageSource bricked(@Nullable Entity attacker) {
        return new BrickedDamageSource(attacker);
    }

    private static class BrickedDamageSource extends DamageSource {
        private final Entity attacker;

        public BrickedDamageSource(Entity attacker) {
            super("andromeda_bricked");
            this.attacker = attacker;
        }

        @Override
        public Text getDeathMessage(LivingEntity entity) {
            return TextUtil.translatable("death.attack.andromeda_bricked", entity.getDisplayName(), attacker != null ? attacker.getDisplayName() : "");
        }

        @Nullable
        @Override
        public Entity getAttacker() {
            return attacker;
        }
    }
}

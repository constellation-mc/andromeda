package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
import me.melontini.andromeda.registries.Keeper;
import me.melontini.dark_matter.api.content.RegistryUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import static me.melontini.andromeda.registries.Common.id;
import static me.melontini.andromeda.util.CommonValues.MODID;

public class Content {

    public static final Keeper<EntityType<FlyingItemEntity>> FLYING_ITEM = Keeper.of(() -> () ->
            RegistryUtil.createEntityType(() -> ModuleManager.quick(ThrowableItems.class).config().enabled,
                    id("flying_item"),
                    FabricEntityTypeBuilder.<FlyingItemEntity>create(SpawnGroup.MISC, FlyingItemEntity::new)
                            .dimensions(new EntityDimensions(0.25F, 0.25F, true)).trackRangeChunks(4).trackedUpdateRate(10)));

    public static final Identifier FLYING_STACK_LANDED = new Identifier(MODID, "flying_stack_landed");
    public static final Identifier ITEMS_WITH_BEHAVIORS = new Identifier(MODID, "items_with_behaviors");
    public static final Identifier COLORED_FLYING_STACK_LANDED = new Identifier(MODID, "colored_flying_stack_landed");

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var packet = PacketByteBufs.create();
            var items = ItemBehaviorManager.itemsWithBehaviors();
            packet.writeVarInt(items.size());
            for (Item item : items) {
                packet.writeIdentifier(Registry.ITEM.getId(item));
            }
            sender.sendPacket(ITEMS_WITH_BEHAVIORS, packet);
        });
    }

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

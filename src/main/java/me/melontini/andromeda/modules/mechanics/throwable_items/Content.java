package me.melontini.andromeda.modules.mechanics.throwable_items;

import me.melontini.andromeda.common.conflicts.CommonRegistries;
import me.melontini.andromeda.common.registries.Keeper;
import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
import me.melontini.dark_matter.api.content.RegistryUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Position;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static me.melontini.andromeda.common.registries.Common.id;
import static me.melontini.andromeda.util.CommonValues.MODID;

public class Content {

    public static final Keeper<EntityType<FlyingItemEntity>> FLYING_ITEM = Keeper.of(() ->
            RegistryUtil.createEntityType(id("flying_item"),
                    FabricEntityTypeBuilder.<FlyingItemEntity>create(SpawnGroup.MISC, FlyingItemEntity::new)
                            .dimensions(new EntityDimensions(0.25F, 0.25F, true)).trackRangeChunks(4).trackedUpdateRate(10)));

    public static final Identifier FLYING_STACK_LANDED = new Identifier(MODID, "flying_stack_landed");
    public static final Identifier ITEMS_WITH_BEHAVIORS = new Identifier(MODID, "items_with_behaviors");
    public static final Identifier COLORED_FLYING_STACK_LANDED = new Identifier(MODID, "colored_flying_stack_landed");

    public static final ProjectileDispenserBehavior BEHAVIOR = new ProjectileDispenserBehavior() {
        @Override
        protected ProjectileEntity createProjectile(World world, Position position, ItemStack stack) {
            ItemStack stack1 = stack.copy();
            stack1.setCount(1);
            return new FlyingItemEntity(stack1, position.getX(), position.getY(), position.getZ(), world);
        }
    };

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var packet = sendItemsS2CPacket();
            sender.sendPacket(ITEMS_WITH_BEHAVIORS, packet);
        });
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
            var packet = sendItemsS2CPacket();
            for (ServerPlayerEntity player : PlayerLookup.all(server)) {
                ServerPlayNetworking.send(player, ITEMS_WITH_BEHAVIORS, packet);
            }
        });
    }

    private static PacketByteBuf sendItemsS2CPacket() {
        var packet = PacketByteBufs.create();
        var items = ItemBehaviorManager.itemsWithBehaviors();
        packet.writeVarInt(items.size());
        for (Item item : items) {
            packet.writeIdentifier(CommonRegistries.items().getId(item));
        }
        return packet;
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

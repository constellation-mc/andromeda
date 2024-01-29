package me.melontini.andromeda.modules.items.pouches.items;

import lombok.Getter;
import me.melontini.andromeda.common.util.WorldUtil;
import me.melontini.andromeda.modules.items.pouches.Main;
import me.melontini.andromeda.modules.items.pouches.entities.PouchEntity;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class PouchItem extends Item {

    private final PouchEntity.Type type;

    public PouchItem(PouchEntity.Type type, Settings settings) {
        super(settings);
        this.type = type;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (context.isAdvanced() && Debug.hasKey(Debug.Keys.DISPLAY_TRACKED_VALUES)) {
            tooltip.add(TextUtil.literal("Loot: " + this.getType().getLootId(stack)).formatted(Formatting.GRAY));
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.random.nextFloat() * 0.4F + 0.8F));
        if (!world.isClient) {
            var entity = new PouchEntity(user, world);
            entity.setPouchType(this.type);
            entity.setPos(user.getX(), user.getEyeY() - 0.1F, user.getZ());
            entity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5F, 1.0F);
            entity.setItem(itemStack);
            world.spawnEntity(entity);
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        if (!user.getAbilities().creativeMode) {
            itemStack.decrement(1);
        }

        return TypedActionResult.success(itemStack, world.isClient());
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!user.getWorld().isClient()) {
            var stacks = WorldUtil.prepareLoot(user.getWorld(), type.getLootId(stack));

            boolean success = false;
            if (entity instanceof PlayerEntity player) {
                stacks.forEach(itemStack -> player.getInventory().offerOrDrop(itemStack));
                success = true;
            } else if (entity instanceof InventoryOwner io) {
                stacks.forEach(itemStack -> Main.tryInsertItem(entity.getWorld(), entity.getPos(), itemStack, io.getInventory()));
                success = true;
            }

            if (success) {
                if (user.getWorld() instanceof ServerWorld sw) {
                    sw.spawnParticles(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), entity.getX(), entity.getY(), entity.getZ(), 10, 0.2, 0.2, 0.2, 0.25);
                }

                if (!user.getAbilities().creativeMode) {
                    stack.decrement(1);
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
}

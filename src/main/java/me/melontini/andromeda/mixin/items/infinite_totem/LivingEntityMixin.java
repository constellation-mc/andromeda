package me.melontini.andromeda.mixin.items.infinite_totem;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.networks.AndromedaPackets;
import me.melontini.andromeda.registries.ItemRegistry;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.minecraft.world.PlayerUtil;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(LivingEntity.class)
@Feature("totemSettings.enableInfiniteTotem")
abstract class LivingEntityMixin extends Entity {

    @Shadow public abstract ItemStack getStackInHand(Hand hand);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyExpressionValue(method = "tryUseTotem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean andromeda$infiniteFallback(boolean original, DamageSource source, @Local(index = 3) ItemStack itemStack) {
        return original || itemStack.isOf(ItemRegistry.get().INFINITE_TOTEM.get());
    }

    @WrapWithCondition(method = "tryUseTotem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    private boolean andromeda$infiniteFallback(ItemStack instance, int i) {
        return !instance.isOf(ItemRegistry.get().INFINITE_TOTEM.get());
    }

    @SuppressWarnings("InvalidInjectorMethodSignature")
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;sendEntityStatus(Lnet/minecraft/entity/Entity;B)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILSOFT, method = "tryUseTotem", cancellable = true)
    private void andromeda$useInfiniteTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir, ItemStack itemStack) {
        if (Config.get().totemSettings.enableInfiniteTotem) {
            if (itemStack.isOf(ItemRegistry.get().INFINITE_TOTEM.get())) {
                if (!world.isClient()) {
                    PacketByteBuf buf = PacketByteBufs.create()
                            .writeUuid(this.getUuid())
                            .writeItemStack(new ItemStack(ItemRegistry.get().INFINITE_TOTEM.get()));
                    buf.writeIdentifier(Registry.PARTICLE_TYPE.getId(Andromeda.get().KNOCKOFF_TOTEM_PARTICLE));

                    for (PlayerEntity player : PlayerUtil.findPlayersInRange(world, getBlockPos(), 120)) {
                        ServerPlayNetworking.send((ServerPlayerEntity) player, AndromedaPackets.USED_CUSTOM_TOTEM, buf);
                    }
                }
                cir.setReturnValue(true);
            }
        }
    }
}

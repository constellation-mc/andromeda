package me.melontini.andromeda.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootContextTypes.class)
abstract class LootContextTypesMixin {

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;ofVanilla(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"), method = "register")
    private static Identifier identifierOfAnything(String path, Operation<Identifier> original) {
        return Identifier.of(path);
    }
}

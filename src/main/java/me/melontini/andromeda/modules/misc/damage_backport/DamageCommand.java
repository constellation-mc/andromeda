package me.melontini.andromeda.modules.misc.damage_backport;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.entity.damage.ProjectileDamageSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class DamageCommand {
    private static final SimpleCommandExceptionType INVULNERABLE_EXCEPTION = new SimpleCommandExceptionType(TextUtil.translatable("commands.andromeda.damage.invulnerable"));

    public DamageCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            DamageCommand.register(dispatcher);
        });
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((CommandManager.literal("damage").requires(source -> source.hasPermissionLevel(2)))
                .then(CommandManager.argument("target", EntityArgumentType.entity())
                        .then((CommandManager.argument("amount", FloatArgumentType.floatArg(0.0F))
                                .executes(context -> execute(
                                        context.getSource(),
                                        EntityArgumentType.getEntity(context, "target"),
                                        FloatArgumentType.getFloat(context, "amount"),
                                        DamageSource.GENERIC)))
                                .then(CommandManager.literal("by")
                                        .then((CommandManager.argument("type", StringArgumentType.string())
                                                        .executes(context -> execute(
                                                                context.getSource(),
                                                                EntityArgumentType.getEntity(context, "target"),
                                                                FloatArgumentType.getFloat(context, "amount"),
                                                                new DamageSource(StringArgumentType.getString(context, "type"))
                                                        ))
                                                        .then(CommandManager.argument("source", EntityArgumentType.entity())
                                                                .executes(context -> execute(
                                                                        context.getSource(),
                                                                        EntityArgumentType.getEntity(context, "target"),
                                                                        FloatArgumentType.getFloat(context, "amount"),
                                                                        new EntityDamageSource(
                                                                                StringArgumentType.getString(context, "type"),
                                                                                EntityArgumentType.getEntity(context, "source"))
                                                                ))
                                                                .then(CommandManager.argument("cause", EntityArgumentType.entity())
                                                                        .executes(context -> execute(
                                                                                context.getSource(),
                                                                                EntityArgumentType.getEntity(context, "target"),
                                                                                FloatArgumentType.getFloat(context, "amount"),
                                                                                new ProjectileDamageSource(
                                                                                        StringArgumentType.getString(context, "type"),
                                                                                        EntityArgumentType.getEntity(context, "cause"),
                                                                                        EntityArgumentType.getEntity(context, "source"))
                                                                        ))
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int execute(ServerCommandSource source, Entity target, float amount, DamageSource damageSource) throws CommandSyntaxException {
        if (target.damage(damageSource, amount)) {
            source.sendFeedback(TextUtil.translatable("commands.andromeda.damage.success", amount, target.getDisplayName()), true);
            return 1;
        } else {
            throw INVULNERABLE_EXCEPTION.create();
        }
    }
}

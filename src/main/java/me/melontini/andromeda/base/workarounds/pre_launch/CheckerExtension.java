package me.melontini.andromeda.base.workarounds.pre_launch;

import com.llamalad7.mixinextras.utils.MixinInternals;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

public class CheckerExtension implements IExtension {

    public static void add() {
        MixinInternals.registerExtension(new CheckerExtension());
    }

    @Override
    public boolean checkActive(MixinEnvironment environment) {
        return true;
    }

    @Getter @Accessors(fluent = true)
    static boolean done = false;

    @Override
    public void preApply(ITargetClassContext context) {
        done = true;
    }

    @Override
    public void postApply(ITargetClassContext context) {

    }

    @Override
    public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {

    }
}

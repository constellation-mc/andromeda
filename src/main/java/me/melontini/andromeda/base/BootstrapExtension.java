package me.melontini.andromeda.base;

import com.llamalad7.mixinextras.utils.MixinInternals;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

public class BootstrapExtension implements IExtension {

    public static void add() {
        MixinInternals.registerExtension(new BootstrapExtension());
    }

    static boolean done = false;
    @Override
    public boolean checkActive(MixinEnvironment environment) {
        if (!done) {
            Bootstrap.onPreLaunch();
            done = true;
        }
        return false;
    }

    @Override public void preApply(ITargetClassContext context) {}
    @Override public void postApply(ITargetClassContext context) {}
    @Override public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {}
}

package me.melontini.andromeda.util.mixin;

import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.config.Config;
import me.melontini.dark_matter.api.base.reflect.wrappers.GenericField;
import me.melontini.dark_matter.api.base.util.Utilities;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import java.util.ArrayList;
import java.util.List;

public class InitExtension implements IExtension {

    private static boolean done = false;

    private static final GenericField<Extensions, List<IExtension>> EXTENSIONS = Utilities.consume(GenericField.of(Extensions.class, "extensions"), f -> f.accessible(true));
    private static final GenericField<Extensions, List<IExtension>> ACTIVE_EXTENSIONS = Utilities.consume(GenericField.of(Extensions.class, "activeExtensions"), f -> f.accessible(true));

    public static void addExtension(IExtension extension) {
        Extensions extensions = (Extensions) ((IMixinTransformer) MixinEnvironment.getDefaultEnvironment().getActiveTransformer()).getExtensions();

        List<IExtension> list = EXTENSIONS.get(extensions);
        list.add(extension);

        List<IExtension> active = new ArrayList<>(ACTIVE_EXTENSIONS.get(extensions));
        active.add(extension);
        ACTIVE_EXTENSIONS.set(extensions, active);
    }

    @Override
    public boolean checkActive(MixinEnvironment environment) {
        if (!done) {
            Config.get();

            ModuleManager.get().prepare();
            ModuleManager.get().print();

            ModuleManager.onPreLaunch();
            done = true;
        }
        return false; //We don't need other methods
    }

    @Override
    public void preApply(ITargetClassContext context) {}
    @Override
    public void postApply(ITargetClassContext context) {}
    @Override
    public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {}
}

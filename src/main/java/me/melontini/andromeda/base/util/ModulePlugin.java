package me.melontini.andromeda.base.util;

import me.melontini.andromeda.base.MixinProcessor;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.andromeda.util.mixin.AndromedaMixins;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;
import me.melontini.dark_matter.api.base.util.mixin.IPluginPlugin;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import org.spongepowered.asm.util.Annotations;

import java.util.List;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class ModulePlugin extends ExtendablePlugin {

    private static final String MIXIN_ENVIRONMENT_ANNOTATION = "L" + SpecialEnvironment.class.getName().replace(".", "/") + ";";

    private String mixinPackage;
    private final MixinProcessor processor = ModuleManager.get().getMixinProcessor();

    @Override
    protected void collectPlugins(Set<IPluginPlugin> plugins) {
        plugins.add(DefaultPlugins.constructDummyPlugin());
    }

    protected void onPluginLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage;
    }

    protected void getMixins(List<String> mixins) {
        mixins.addAll(AndromedaMixins.discoverInPackage(this.mixinPackage));
    }

    @Override
    protected void afterApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (targetClass.visibleAnnotations != null && !targetClass.visibleAnnotations.isEmpty()) {//strip our annotation from the class
            targetClass.visibleAnnotations.removeIf(node -> MIXIN_ENVIRONMENT_ANNOTATION.equals(node.desc));
        }

        for (MethodNode method : targetClass.methods) {
            AnnotationNode unique = Annotations.getVisible(method, Unique.class);
            AnnotationNode mixinMerged = Annotations.getVisible(method, MixinMerged.class);
            if (unique == null && mixinMerged != null) {
                String mixin = Annotations.getValue(mixinMerged, "mixin");
                if (mixin.startsWith(this.mixinPackage)) {
                    wrapNodeWithErrorHandling(method, processor.fromConfig(mixinInfo.getConfig().getName()).orElseThrow().meta().id());
                }
            }
        }
    }

    private void wrapNodeWithErrorHandling(MethodNode handlerNode, String module) {
        Label start = new Label(), end = new Label(), handler = new Label(), handlerEnd = new Label();

        String throwable = Type.getInternalName(Throwable.class);
        handlerNode.visitTryCatchBlock(start, end, handler, throwable);

        InsnList old = handlerNode.instructions;
        handlerNode.instructions = new InsnList();
        handlerNode.visitLabel(start);
        handlerNode.instructions.add(old);

        handlerNode.visitLabel(end);
        handlerNode.visitJumpInsn(Opcodes.GOTO, handlerEnd);

        handlerNode.visitLabel(handler);
        handlerNode.visitVarInsn(Opcodes.ASTORE, handlerNode.maxLocals);

        handlerNode.visitVarInsn(Opcodes.ALOAD, handlerNode.maxLocals);
        handlerNode.visitLdcInsn(module);
        handlerNode.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(AndromedaException.class), "moduleException", "(" + Type.getDescriptor(Throwable.class) + Type.getDescriptor(String.class) + ")" + Type.getDescriptor(AndromedaException.class), false);

        handlerNode.visitInsn(Opcodes.ATHROW);
        handlerNode.visitLabel(handlerEnd);

        handlerNode.visitLocalVariable("exc", "L" + throwable + ";", null, start, handler, handlerNode.maxLocals);
    }
}

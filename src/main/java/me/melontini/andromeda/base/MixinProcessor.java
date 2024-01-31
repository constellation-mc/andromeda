package me.melontini.andromeda.base;

import com.google.gson.JsonObject;
import lombok.CustomLog;
import me.melontini.andromeda.base.annotations.SpecialEnvironment;
import me.melontini.andromeda.base.events.Bus;
import me.melontini.andromeda.base.events.MixinConfigEvent;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.andromeda.util.mixin.AndromedaMixins;
import me.melontini.dark_matter.api.base.reflect.wrappers.GenericField;
import me.melontini.dark_matter.api.base.reflect.wrappers.GenericMethod;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;
import me.melontini.dark_matter.api.base.util.mixin.IPluginPlugin;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.FabricUtil;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.meta.MixinMerged;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

/**
 * The MixinProcessor is responsible for injecting dynamic mixin configs.
 * <p> This is done by creating a config with GSON, passing it to the {@link ByteArrayInputStream}, and injecting this input stream to our temporary fake mixin service using a {@link ThreadLocal}.
 * <p> This must be done during {@code 'preLaunch'} as no classes should be transformed at this point.
 */
@CustomLog
public class MixinProcessor {

    public static final String NOTICE = "## Mixin configs are internal mod components and are not the same as user configs! ##";
    private static final ThreadLocal<InputStream> CONFIG = ThreadLocal.withInitial(() -> null);
    private static boolean done = false;

    public static void addMixins(ModuleManager manager) {
        if (done) return;
        IMixinService service = MixinService.getService();
        MixinProcessor.injectService(service);
        manager.loaded().forEach((module) -> {
            JsonObject config = createConfig(module);

            String cfg = "andromeda_dynamic$$" + module.meta().dotted() + ".mixins.json";
            try (ByteArrayInputStream bais = new ByteArrayInputStream(config.toString().getBytes())) {
                CONFIG.set(bais);
                Mixins.addConfiguration(cfg);
                manager.mixinConfigs.put(cfg, module);
            } catch (IOException e) {
                throw AndromedaException.builder()
                        .message("Couldn't inject mixin config for module '%s'".formatted(module.meta().id())).message(NOTICE)
                        .add("mixin_config", cfg).add("module", module.meta().id()).build();
            } finally {
                CONFIG.remove();
            }
        });
        MixinProcessor.dejectService(service);
        done = true;

        Mixins.getConfigs().forEach(config1 -> {
            if (manager.mixinConfigs.containsKey(config1.getName())) {
                config1.getConfig().decorate(FabricUtil.KEY_MOD_ID, "andromeda");
            }
        });
    }

    public static JsonObject createConfig(Module<?> module) {
        JsonObject object = new JsonObject();
        object.addProperty("required", true);
        object.addProperty("minVersion", "0.8");
        object.addProperty("package", module.getClass().getPackageName() + ".mixin");
        object.addProperty("compatibilityLevel", "JAVA_17");
        object.addProperty("plugin", Plugin.class.getName());
        object.addProperty("refmap", "andromeda-refmap.json");
        JsonObject injectors = new JsonObject();
        injectors.addProperty("defaultRequire", 1);
        object.add("injectors", injectors);

        Bus<MixinConfigEvent> bus = module.getOrCreateBus(MixinConfigEvent.class, null);
        if (bus != null) bus.invoker().accept(object);

        return object;
    }

    private static final GenericMethod<?, MixinService> GET_INSTANCE = GenericMethod.of(MixinService.class, "getInstance");
    private static final GenericField<MixinService, IMixinService> SERVICE = GenericField.of(MixinService.class, "service");

    public static void injectService(IMixinService currentService) {
        IMixinService service = (IMixinService) Proxy.newProxyInstance(MixinProcessor.class.getClassLoader(), new Class[]{IMixinService.class}, (proxy, method, args) -> {
            if (method.getName().equals("getResourceAsStream")) {
                if (args[0] instanceof String s) {
                    if (s.startsWith("andromeda_dynamic$$")) {
                        return CONFIG.get();
                    }
                }
            }

            return method.invoke(currentService, args);
        });
        MixinService serviceProxy = GET_INSTANCE.accessible(true).invoke(null);
        SERVICE.accessible(true).set(serviceProxy, service);
    }

    public static void dejectService(IMixinService realService) {
        MixinService serviceProxy = GET_INSTANCE.accessible(true).invoke(null);
        SERVICE.accessible(true).set(serviceProxy, realService);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class Plugin extends ExtendablePlugin {

        private static final String MIXIN_ENVIRONMENT_ANNOTATION = "L" + SpecialEnvironment.class.getName().replace(".", "/") + ";";

        private String mixinPackage;

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
                        wrapNodeWithErrorHandling(method, ModuleManager.get().moduleFromConfig(mixinInfo.getConfig().getName()).orElseThrow().meta().id());
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
}

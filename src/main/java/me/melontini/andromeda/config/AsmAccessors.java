package me.melontini.andromeda.config;

import lombok.CustomLog;
import me.melontini.dark_matter.api.base.util.mixin.AsmUtil;
import me.melontini.dark_matter.api.config.ConfigBuilder;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CustomLog
public class AsmAccessors {

    private static final Map<String, AccessorProxy> PROXY_MAP = new HashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    private static final String ITF = Type.getInternalName(AccessorProxy.class);
    private static final String CONF_DESC = Type.getDescriptor(AndromedaConfig.class);

    private static final Map<Class<?>, String> WRAPPER_TO_PRIMITIVE = Map.of(
            boolean.class, "booleanValue",
            byte.class, "byteValue",
            short.class, "shortValue",
            int.class, "intValue",
            long.class, "longValue",
            float.class, "floatValue",
            double.class, "doubleValue",
            char.class, "charValue"
    );

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            char.class, Character.class,
            void.class, void.class
    );

    @SuppressWarnings("UnstableApiUsage")
    static void attach(ConfigBuilder<AndromedaConfig> builder) {
        builder.scanner((cls, currentField, parentString, recursive, fieldRefView) -> generateAccessors(parentString, fieldRefView))
                .getter((configManager, option) -> {
                    if (!PROXY_MAP.containsKey(option)) throw new NoSuchFieldException(option);
                    return PROXY_MAP.get(option).get();
                })
                .setter((manager, option, value) -> {
                    if (!PROXY_MAP.containsKey(option)) throw new NoSuchFieldException(option);
                    PROXY_MAP.get(option).set(value);
                });
    }

    private static void generateAccessors(String parentString, List<Field> fieldRef) {
        Field field = fieldRef.get(fieldRef.size() - 1);
        String currentFieldName = (parentString + field.getName());

        Class<?> type = field.getType();
        if (type.isPrimitive()) {
            type = toWrapper(type);
        }
        String clsName = "me/melontini/andromeda/config/DynamicAccessors$" + currentFieldName.replace(".", "$");
        ClassNode node = new ClassNode();
        node.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER | Opcodes.ACC_FINAL, clsName, null, Type.getInternalName(Object.class), new String[]{ITF});

        AsmUtil.insAdapter(node, Opcodes.ACC_PUBLIC, "<init>", "()V", ia -> {
            ia.load(0, Type.getType(Object.class));
            ia.invokespecial(Type.getInternalName(Object.class), "<init>", "()V", false);
            ia.areturn(Type.VOID_TYPE);
        });

        addGet(node, field, type, fieldRef);
        addSet(node, field, type, fieldRef);

        try {
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            node.accept(cw);
            AccessorProxy proxy = (AccessorProxy) LOOKUP.defineClass(cw.toByteArray()).getConstructor().newInstance();
            PROXY_MAP.put(parentString + field.getName(), proxy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addSet(ClassNode node, Field field, Class<?> type, List<Field> fieldRef) {
        AsmUtil.insAdapter(node, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "set", "(Ljava/lang/Object;)V", ia -> {
            ia.invokestatic(Type.getInternalName(Config.class), "get", "()" + CONF_DESC, false);
            for (int i = 0; i < fieldRef.size() - 1; i++) {
                Field f = fieldRef.get(i);
                ia.getfield(Type.getInternalName(f.getDeclaringClass()), f.getName(), Type.getDescriptor(f.getType()));
            }
            ia.load(1, Type.getType(Object.class));
            ia.checkcast(Type.getType(type));
            if (field.getType().isPrimitive()) {
                ia.invokevirtual(Type.getInternalName(type), fromWrapperMethod(field.getType()), "()" + Type.getDescriptor(field.getType()), false);
            }
            ia.putfield(Type.getInternalName(field.getDeclaringClass()), field.getName(), Type.getDescriptor(field.getType()));
            ia.areturn(Type.VOID_TYPE);
        });
    }

    private static void addGet(ClassNode node, Field field, Class<?> type, List<Field> fieldRef) {
        AsmUtil.insAdapter(node, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "get", "()Ljava/lang/Object;", ia -> {
            ia.invokestatic(Type.getInternalName(Config.class), "get", "()" + CONF_DESC, false);
            for (Field f : fieldRef) {
                ia.getfield(Type.getInternalName(f.getDeclaringClass()), f.getName(), Type.getDescriptor(f.getType()));
            }
            if (field.getType().isPrimitive()) {
                ia.invokestatic(Type.getInternalName(type), "valueOf", "(" + Type.getDescriptor(field.getType()) + ")" + Type.getDescriptor(type), false);
            }
            ia.areturn(Type.getType(Object.class));
        });
    }

    private static String fromWrapperMethod(Class<?> cls) {
        return WRAPPER_TO_PRIMITIVE.getOrDefault(cls, null);
    }

    private static Class<?> toWrapper(Class<?> cls) {
        return PRIMITIVE_TO_WRAPPER.getOrDefault(cls, cls);
    }
}

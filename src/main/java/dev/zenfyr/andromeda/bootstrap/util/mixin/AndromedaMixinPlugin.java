package dev.zenfyr.andromeda.bootstrap.util.mixin;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import dev.zenfyr.andromeda.bootstrap.util.Util;
import dev.zenfyr.pulsar.api.mixin.AsmUtil;
import dev.zenfyr.pulsar.api.platform.CEnvType;
import dev.zenfyr.pulsar.api.platform.Platform;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

public class AndromedaMixinPlugin implements IMixinConfigPlugin {

  private static final Set<String> CLOTH_MIXINS = Set.of(
      "dev.zenfyr.andromeda.common.mixin.SubCategoryListEntryMixin",
      "dev.zenfyr.andromeda.common.mixin.MultiElementListEntryAccessor");

  public static boolean shouldApply(String mixinClassName) {
    if (CLOTH_MIXINS.contains(mixinClassName)
        && !Platform.getPlatform().isModLoaded("cloth-config")) return false;

    try {
      var node = getClassNode(mixinClassName);
      return AndromedaMixinPlugin.testMixinEnvironment(node);
    } catch (ClassNotFoundException | IOException e) {
      throw Util.wrap(mixinClassName, e);
    }
  }

  private static Boolean isNewProvider = null;

  public static ClassNode getClassNode(String className)
      throws IOException, ClassNotFoundException {
    var provider = MixinService.getService().getBytecodeProvider();
    var normalized = className.replace('.', '/');

    if (isNewProvider != null) {
      if (isNewProvider) {
        return provider.getClassNode(
            normalized,
            false,
            ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
      } else {
        return provider.getClassNode(normalized);
      }
    }

    ClassNode node;
    try {
      node = provider.getClassNode(
          normalized,
          false,
          ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
      isNewProvider = true;
    } catch (ClassNotFoundException | IOException e) {
      throw e; // rethrow expected
    } catch (Throwable e) {
      node = provider.getClassNode(normalized);
      isNewProvider = false;
    }
    return node;
  }

  public static boolean testMixinEnvironment(ClassNode node) {
    var current = Platform.getPlatform().getEnvironment();
    AnnotationNode envNode = Annotations.getInvisible(node, MixinEnvironment.class);
    if (envNode != null) {
      CEnvType value = AsmUtil.getAnnotationValue(envNode, "value", null);
      return current == value;
    }
    return true;
  }

  public static void postApply(ClassNode targetClass) {
    if (targetClass.invisibleAnnotations != null
        && !targetClass.invisibleAnnotations.isEmpty()) { // strip our annotation from the class
      targetClass.invisibleAnnotations.removeIf(
          node -> MixinEnvironment.MIXIN_ENVIRONMENT_ANNOTATION.equals(node.desc));
    }
  }

  @Override
  public void onLoad(String mixinPackage) {
    ModuleManager.tryInit();
  }

  @Override
  public String getRefMapperConfig() {
    return null;
  }

  @Override
  public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
    return shouldApply(mixinClassName);
  }

  @Override
  public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

  @Override
  public List<String> getMixins() {
    return List.of();
  }

  @Override
  public void preApply(
      String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

  @Override
  public void postApply(
      String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    AndromedaMixinPlugin.postApply(targetClass);
  }
}

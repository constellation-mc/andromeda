package dev.zenfyr.andromeda.bootstrap.util.mixin;

import static dev.zenfyr.andromeda.util.AndromedaConstants.MODID;

import com.google.common.collect.ImmutableList;
import dev.zenfyr.andromeda.util.ClassPath;
import dev.zenfyr.andromeda.util.Util;
import dev.zenfyr.pulsar.api.mixin.AsmUtil;
import dev.zenfyr.pulsar.api.platform.Platform;
import dev.zenfyr.pulsar.api.util.ExceptionUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.Annotations;

public class AndromedaMixinPlugin implements IMixinConfigPlugin {

  public static final ClassPath CLASS_PATH = new ClassPath();
  private static final Set<String> CLOTH_MIXINS =
      Set.of("SubCategoryListEntryMixin", "MultiElementListEntryAccessor");

  private String mixinPackage;

  public static List<String> discoverInPackage(String pck) {
    return CLASS_PATH.getTopLevelRecursive(pck).stream()
        .map(info -> {
          ClassReader reader = new ClassReader(ExceptionUtil.supply(info::readAllBytes));
          ClassNode node = new ClassNode();
          reader.accept(
              node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
          return node;
        })
        .filter(AndromedaMixinPlugin::testMixinEnvironment)
        .map((n) -> n.name.replace('/', '.').substring((pck + ".").length()))
        .collect(ImmutableList.toImmutableList());
  }

  public static boolean testMixinEnvironment(ClassNode node) {
    var current = FabricLoader.getInstance().getEnvironmentType();
    AnnotationNode envNode = Annotations.getInvisible(node, MixinEnvironment.class);
    if (envNode != null) {
      EnvType value = AsmUtil.getAnnotationValue(envNode, "value", null);
      return current == value;
    }
    return true;
  }

  @Override
  public void onLoad(String mixinPackage) {
    this.mixinPackage = mixinPackage;

    CLASS_PATH.addPaths(
        FabricLoader.getInstance().getModContainer(MODID).orElseThrow().getRootPaths());

    if (!Files.exists(Util.HIDDEN_PATH)) {
      try {
        Files.createDirectories(Util.HIDDEN_PATH);
        if (Util.HIDDEN_PATH.getFileSystem().supportedFileAttributeViews().contains("dos"))
          Files.setAttribute(
              Util.HIDDEN_PATH, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public String getRefMapperConfig() {
    return "";
  }

  @Override
  public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
    return true;
  }

  @Override
  public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

  @Override
  public List<String> getMixins() {
    return discoverInPackage(this.mixinPackage).stream()
        .filter(mixinClassName -> !CLOTH_MIXINS.contains(mixinClassName)
            || Platform.getPlatform().isModLoaded("cloth-config"))
        .toList();
  }

  @Override
  public void preApply(
      String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

  @Override
  public void postApply(
      String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}

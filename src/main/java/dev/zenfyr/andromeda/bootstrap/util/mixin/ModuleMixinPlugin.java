package dev.zenfyr.andromeda.bootstrap.util.mixin;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class ModuleMixinPlugin implements IMixinConfigPlugin {

  private static final String MIXIN_ENVIRONMENT_ANNOTATION =
      "L" + MixinEnvironment.class.getName().replace(".", "/") + ";";

  private String mixinPackage;
  private final MixinHandler processor = ModuleManager.get().mixinHandler();

  @Override
  public void onLoad(String mixinPackage) {
    this.mixinPackage = mixinPackage;
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
    return processor.mixinsFromPackage(this.mixinPackage);
  }

  @Override
  public void preApply(
      String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

  @Override
  public void postApply(
      String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    if (targetClass.visibleAnnotations != null
        && !targetClass.visibleAnnotations.isEmpty()) { // strip our annotation from the class
      targetClass.visibleAnnotations.removeIf(
          node -> MIXIN_ENVIRONMENT_ANNOTATION.equals(node.desc));
    }
  }
}

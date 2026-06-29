package dev.zenfyr.andromeda.bootstrap.util.mixin;

import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class ModuleMixinPlugin implements IMixinConfigPlugin {

  private String mixinPackage;
  private ModuleManager manager;

  @Override
  public void onLoad(String mixinPackage) {
    ModuleManager.tryInit();

    this.mixinPackage = mixinPackage;
    this.manager = ModuleManager.get();
  }

  @Override
  public String getRefMapperConfig() {
    return null;
  }

  @Override
  public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
    return this.manager.shouldApplyMixin(this.mixinPackage, mixinClassName);
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

package dev.zenfyr.andromeda.bootstrap.util.mixin;

import java.util.List;
import java.util.Set;
import dev.zenfyr.andromeda.bootstrap.ModuleManager;
import me.melontini.dark_matter.api.mixin.ExtendablePlugin;
import me.melontini.dark_matter.api.mixin.IPluginPlugin;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class ModuleMixinPlugin extends ExtendablePlugin {

  private static final String MIXIN_ENVIRONMENT_ANNOTATION =
      "L" + MixinEnvironment.class.getName().replace(".", "/") + ";";

  private String mixinPackage;
  private final MixinHandler processor = ModuleManager.get().mixinHandler();

  @Override
  protected void onPluginLoad(String mixinPackage) {
    this.mixinPackage = mixinPackage;
  }

  @Override
  protected void getMixins(List<String> mixins) {
    mixins.addAll(processor.mixinsFromPackage(this.mixinPackage));
  }

  @Override
  protected void collectPlugins(Set<IPluginPlugin> plugins) {
    plugins.add(DefaultPlugins.constructDummyPlugin());
  }

  @Override
  protected void afterApply(
      String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    if (targetClass.visibleAnnotations != null
        && !targetClass.visibleAnnotations.isEmpty()) { // strip our annotation from the class
      targetClass.visibleAnnotations.removeIf(
          node -> MIXIN_ENVIRONMENT_ANNOTATION.equals(node.desc));
    }
  }
}

package me.melontini.andromeda.modules.world.auto_planting;

import com.google.common.collect.Lists;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Origin;

import java.util.List;

@Origin(mod = "TinyTweaks", author = "HephaestusDev")
@ModuleInfo(name = "auto_planting", category = "world", environment = Environment.SERVER)
public class AutoPlanting extends Module<AutoPlanting.Config> {

    AutoPlanting() {
    }

    public static class Config extends BaseConfig {

        public boolean blacklistMode = true;

        public List<String> idList = Lists.newArrayList();
    }
}

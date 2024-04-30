package me.melontini.andromeda.modules.items.lockpick;

import lombok.ToString;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.annotations.Unscoped;
import me.melontini.andromeda.modules.items.lockpick.client.Client;
import me.melontini.andromeda.util.commander.bool.BooleanIntermediary;
import me.melontini.andromeda.util.commander.number.NumberIntermediary;

import java.util.List;

@Unscoped
@ModuleInfo(name = "lockpick", category = "items")
public class Lockpick extends Module<Lockpick.Config> {

    Lockpick() {
        InitEvent.main(this).listen(() -> List.of(Main.class));
        InitEvent.client(this).listen(() -> List.of(Client.class));
    }

    @ToString
    public static class Config extends BaseConfig {

        public NumberIntermediary chance = NumberIntermediary.of(3);

        public BooleanIntermediary breakAfterUse = BooleanIntermediary.of(true);

        public boolean villagerInventory = true;
    }
}

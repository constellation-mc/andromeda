package me.melontini.andromeda.common.client;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.Andromeda;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import java.util.concurrent.CompletableFuture;

public class ClientSideNetworking {

    public static void register() {
        ClientLoginNetworking.registerGlobalReceiver(Andromeda.VERIFY_MODULES, (client, handler, buf, listenerAdder) -> {
            String[] modules = ModuleManager.get().loaded().stream().map(Module::meta)
                    .filter(m -> m.environment().isBoth()).map(Module.Metadata::id).toArray(String[]::new);
            var pbf = PacketByteBufs.create();
            pbf.writeVarInt(modules.length);
            for (String module : modules) {
                pbf.writeString(module);
            }
            return CompletableFuture.completedFuture(pbf);
        });
    }
}

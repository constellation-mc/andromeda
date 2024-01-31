package me.melontini.andromeda.common.client;

import me.melontini.andromeda.base.Environment;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.Andromeda;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import java.util.concurrent.CompletableFuture;

public class ClientSideNetworking {



    public static void register() {
        ClientLoginNetworking.registerGlobalReceiver(Andromeda.VERIFY_MODULES, (client, handler, buf, listenerAdder) -> {
            String[] modules = ModuleManager.get().loaded().stream().filter(m -> m.meta().environment() == Environment.BOTH)
                    .map(m -> m.meta().id()).toArray(String[]::new);
            var pbf = PacketByteBufs.create();
            pbf.writeVarInt(modules.length);
            for (String module : modules) {
                pbf.writeString(module);
            }
            return CompletableFuture.completedFuture(pbf);
        });
    }
}

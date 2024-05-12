package me.melontini.andromeda.util;

import com.google.common.base.Suppliers;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import me.melontini.dark_matter.api.base.util.Exceptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

@Log4j2 @UtilityClass
public class CommonValues {

    public static final String MODID = "andromeda";

    private static final Supplier<ModContainer> MOD_CONTAINER = Suppliers.memoize(() -> FabricLoader.getInstance().getModContainer(MODID).orElseThrow());
    private static final Supplier<String> MOD_VERSION = Suppliers.memoize(() -> mod().getMetadata().getVersion().getFriendlyString());
    private static final Supplier<Boolean> MOD_UPDATED = Suppliers.memoize(CommonValues::checkUpdate);

    private static final Supplier<Path> HIDDEN_PATH = Suppliers.memoize(() -> {
        var path = FabricLoader.getInstance().getGameDir().resolve(".andromeda");
        if (!Files.exists(path)) {
            Exceptions.run(() -> Files.createDirectories(path));
            try {
                if (path.getFileSystem().supportedFileAttributeViews().contains("dos"))
                    Files.setAttribute(path, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
            } catch (IOException ignored) {
                LOGGER.warn("Failed to hide the .andromeda folder");
            }
        }
        return path;
    });
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("andromeda/mod.json");

    private static final Supplier<Platform> PLATFORM = Suppliers.memoize(CommonValues::resolvePlatform);
    private static final EnvType ENVIRONMENT = FabricLoader.getInstance().getEnvironmentType();

    public static ModContainer mod() {
        return MOD_CONTAINER.get();
    }
    public static String version() {
        return MOD_VERSION.get();
    }
    public static boolean updated() {
        return MOD_UPDATED.get();
    }

    public static Path hiddenPath() {
        return HIDDEN_PATH.get();
    }
    public static Path configPath() {
        return CONFIG_PATH;
    }

    public static Platform platform() {
        return PLATFORM.get();
    }
    public static EnvType environment() {
        return ENVIRONMENT;
    }

    private static boolean checkUpdate() {
        Path lh = hiddenPath().resolve("last_version.txt");
        if (Files.exists(lh)) {
            Version version = Exceptions.supply(() -> Version.parse(Files.readString(lh)));
            if (mod().getMetadata().getVersion().compareTo(version) != 0) {
                if (!FabricLoader.getInstance().isDevelopmentEnvironment())
                    LOGGER.warn("Andromeda version changed! was [{}], now [{}]", version.getFriendlyString(), mod().getMetadata().getVersion().getFriendlyString());
                Exceptions.run(() -> Files.writeString(lh, mod().getMetadata().getVersion().getFriendlyString()));
                return true;
            }
        } else {
            Exceptions.run(() -> Files.writeString(lh, mod().getMetadata().getVersion().getFriendlyString()));
            return true;
        }
        return false;
    }

    private static Platform resolvePlatform() {
        if (isConnector()) return Platform.CONNECTOR;
        if (FabricLoader.getInstance().isModLoaded(Platform.QUILT.modId)) {
            String sn = MixinService.getService().getName().replaceAll("^Knot|^Launchwrapper|^ModLauncher|/", "");
            if ("quilt".equalsIgnoreCase(sn)) return Platform.QUILT;
        }
        return Platform.FABRIC;
    }

    private static boolean isConnector() {
        if (FabricLoader.getInstance().isModLoaded(Platform.CONNECTOR.modId)) {
            try {
                //The above check should be fine, but just in case.
                Class.forName("dev.su5ed.sinytra.connector.mod.ConnectorMod", false, CommonValues.class.getClassLoader());
                return true;
            } catch (ClassNotFoundException ignored) {
                return false;
            }
        }
        return false;
    }

    public enum Platform {
        FABRIC("fabricloader"),
        CONNECTOR("connectormod"),
        QUILT("quilt_loader"),
        FORGE("forge"),
        NEOFORGE("neoforge") {
            @Override
            public String version() {
                return modVersion(this).orElse(modVersion(FORGE).orElse("0.0.0"));
            }
        };

        final String modId;

        Platform(String modId) {
            this.modId = modId;
        }

        static Optional<String> modVersion(Platform p) {
            return FabricLoader.getInstance().getModContainer(p.modId)
                    .map(container -> container.getMetadata().getVersion().getFriendlyString());
        }

        public String version() {
            return modVersion(this).orElse("0.0.0");
        }

        @Override
        public String toString() {
            return StringUtils.capitalize(name().toLowerCase(Locale.ROOT));
        }
    }
}

package me.melontini.andromeda.util;

import lombok.CustomLog;
import me.melontini.dark_matter.api.base.util.Utilities;
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
import java.util.Optional;

@CustomLog
public class CommonValues {

    public static final String MODID = "andromeda";

    private static final ModContainer MOD_CONTAINER = FabricLoader.getInstance().getModContainer(MODID).orElseThrow();
    private static final String MOD_VERSION = MOD_CONTAINER.getMetadata().getVersion().getFriendlyString();
    private static final boolean MOD_UPDATED;

    private static final Path HIDDEN_PATH = FabricLoader.getInstance().getGameDir().resolve(".andromeda");
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("andromeda/mod.json");

    private static final Platform PLATFORM;
    private static final EnvType ENVIRONMENT = FabricLoader.getInstance().getEnvironmentType();

    public static ModContainer mod() {
        return MOD_CONTAINER;
    }
    public static String version() {
        return MOD_VERSION;
    }
    public static boolean updated() {
        return MOD_UPDATED;
    }

    public static Path hiddenPath() {
        if (!Files.exists(HIDDEN_PATH)) {
            Utilities.runUnchecked(() -> Files.createDirectories(HIDDEN_PATH));
            try {
                if (HIDDEN_PATH.getFileSystem().supportedFileAttributeViews().contains("dos"))
                    Files.setAttribute(HIDDEN_PATH, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
            } catch (IOException ignored) {
                AndromedaLog.warn("Failed to hide the .andromeda folder");
            }
        }
        return HIDDEN_PATH;
    }
    public static Path configPath() {
        return CONFIG_PATH;
    }

    public static Platform platform() {
        return PLATFORM;
    }
    public static EnvType environment() {
        return ENVIRONMENT;
    }

    static {
        PLATFORM = resolvePlatform();
        MOD_UPDATED = checkUpdate();
    }

    private static boolean checkUpdate() {
        Path lh = hiddenPath().resolve("last_version.txt");
        if (Files.exists(lh)) {
            Version version = Utilities.supplyUnchecked(() -> Version.parse(Files.readString(lh)));
            if (mod().getMetadata().getVersion().compareTo(version) != 0) {
                if (!FabricLoader.getInstance().isDevelopmentEnvironment())
                    LOGGER.warn("Andromeda version changed! was [{}], now [{}]", version.getFriendlyString(), mod().getMetadata().getVersion().getFriendlyString());
                Utilities.runUnchecked(() -> Files.writeString(lh, mod().getMetadata().getVersion().getFriendlyString()));
                return true;
            }
        } else {
            Utilities.runUnchecked(() -> Files.writeString(lh, mod().getMetadata().getVersion().getFriendlyString()));
            return true;
        }
        return false;
    }

    private static Platform resolvePlatform() {
        if (FabricLoader.getInstance().isModLoaded(Platform.CONNECTOR.modId)) {
            try {
                //The above check should be fine, but just in case.
                Class.forName("dev.su5ed.sinytra.connector.mod.ConnectorMod");
                return Platform.CONNECTOR;
            } catch (ClassNotFoundException ignored) {
            }
        }
        if (FabricLoader.getInstance().isModLoaded(Platform.QUILT.modId)) {
            String sn = MixinService.getService().getName().replaceAll("^Knot|^Launchwrapper|^ModLauncher|/", "");
            if ("quilt".equalsIgnoreCase(sn)) return Platform.QUILT;
        }
        return Platform.FABRIC;
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
            return StringUtils.capitalize(name().toLowerCase());
        }
    }
}

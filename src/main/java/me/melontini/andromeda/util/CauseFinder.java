package me.melontini.andromeda.util;

import org.spongepowered.asm.mixin.FabricUtil;
import org.spongepowered.asm.mixin.throwables.MixinError;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CauseFinder {

    private static final Pattern FABRIC_ERROR_PATTERN = Pattern.compile(".*due to errors, provided by '(.*)'!");
    private static final Pattern MIXIN_ERROR_PATTERN = Pattern.compile(".*from mod '(.*)'!");

    public static Optional<String> findCause(Throwable cause) {
        if (cause == null) return Optional.empty();
        String modId = tryExtractMixinInfo(cause);
        if (modId == null) modId = fromFabricError(cause);
        if ("(unknown)".equals(modId)) return Optional.empty();
        return Optional.ofNullable(modId);
    }

    private static String fromFabricError(Throwable cause) {
        Matcher matcher = FABRIC_ERROR_PATTERN.matcher(cause.getMessage());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static String tryExtractMixinInfo(Throwable cause) {
        if (cause instanceof InvalidMixinException ime) {
            String modID = extractFromConfig(ime);
            if (modID == null) modID = extractFromConfigFile(ime);
            return modID;
        } else if (cause instanceof MixinError) {
            String modID = extractModIDFromMixinError(cause.getMessage());
            if (modID != null) {
                return modID;
            } else {
                return cause.getCause() == null ? null : tryExtractMixinInfo(cause.getCause());
            }
        }
        return cause.getCause() == null ? null : tryExtractMixinInfo(cause.getCause());
    }

    private static String extractFromConfig(InvalidMixinException ime) {
        try {
            String modID = FabricUtil.getModId(ime.getMixin().getConfig());
            if (!"(unknown)".equals(modID)) return modID;
        } catch (Throwable t) {
            return null;
        }
        return null;
    }

    private static String extractFromConfigFile(InvalidMixinException ime) {
        String[] file = ime.getMixin().getConfig().getName().split("\\.");
        for (String s : file) {
            if (!s.matches("mixin(?:s(?:-\\w+)?|-\\w+)?") && !s.equalsIgnoreCase("json")) return s;
        }
        return null;
    }

    private static String extractModIDFromMixinError(String errorMessage) {
        Matcher matcher = MIXIN_ERROR_PATTERN.matcher(errorMessage);
        if (matcher.find()) return matcher.group(1);
        return null;
    }
}

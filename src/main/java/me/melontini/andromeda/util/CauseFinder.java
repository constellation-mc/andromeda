package me.melontini.andromeda.util;

import org.spongepowered.asm.mixin.FabricUtil;
import org.spongepowered.asm.mixin.throwables.MixinError;
import org.spongepowered.asm.mixin.transformer.throwables.InvalidMixinException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CauseFinder {

    public static String findCause(Throwable cause) {
        if (cause == null) return null;

        return tryExtractMixinInfo(cause);
    }

    private static String tryExtractMixinInfo(Throwable cause) {
        if (cause instanceof InvalidMixinException ime) {
            try {
                String modID = FabricUtil.getModId(ime.getMixin().getConfig());
                if (!"(unknown)".equals(modID)) return modID;
            } catch (Throwable ignored) {}
            String[] file = ime.getMixin().getConfig().getName().split("\\.");
            for (String s : file) {
                if (!s.matches("mixin(?:s(?:-\\w+)?|-\\w+)?") && !s.equalsIgnoreCase("json")) return s;
            }
            return null;
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

    private static String extractModIDFromMixinError(String errorMessage) {
        Matcher matcher = Pattern.compile("from mod (\\w+)").matcher(errorMessage);
        if (matcher.find()) return matcher.group(1);
        return null;
    }
}

package dev.zenfyr.andromeda.util;

import static dev.zenfyr.andromeda.util.AndromedaConstants.MODID;

import com.google.common.base.Splitter;
import dev.zenfyr.pulsar.api.platform.Platform;
import dev.zenfyr.pulsar.api.util.Utilities;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Util {

  private static final Splitter SPLITTER = Splitter.on(".");

  public static final Path HIDDEN_PATH = Platform.getPlatform().getGameDir().resolve("." + MODID);

  public static Logger logger() {
    return LogManager.getLogger(getCaller());
  }

  public static boolean isDev() {
    return Platform.getPlatform().isDev();
  }

  public static RuntimeException create(String msg) {
    return new RuntimeException("(%s) %s".formatted(getCaller(), msg));
  }

  public static RuntimeException create(
      String msg, Function<String, ? extends RuntimeException> factory) {
    return factory.apply("(%s) %s".formatted(getCaller(), msg));
  }

  public static RuntimeException wrap(String msg, Throwable throwable) {
    return new RuntimeException("(%s) %s".formatted(getCaller(), msg), throwable);
  }

  public static <T extends Throwable> void throwIfDev(Supplier<T> throwable) throws T {
    if (isDev()) throw throwable.get();
  }

  public static <T extends Throwable> void throwNowIfDev(T throwable) throws T {
    if (isDev()) throw throwable;
  }

  private static String getCaller() {
    Class<?> cls = Utilities.getCallerClass(3).orElseThrow();
    List<String> split = SPLITTER.splitToList(cls.getName());

    String caller = split.get(split.size() - 1);
    caller = MODID + "/" + caller;
    if (cls.getName().startsWith("net.minecraft.")) caller += "@Mixin";
    return caller;
  }
}

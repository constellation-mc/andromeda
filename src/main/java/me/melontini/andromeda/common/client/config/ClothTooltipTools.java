package me.melontini.andromeda.common.client.config;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.common.client.OrderedTextUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.ArrayUtils;

public class ClothTooltipTools {

  public static boolean checkOptionManager(
      AbstractConfigListEntry<?> e, Module module, String field) {
    var opt = FeatureBlockade.get().explain(ModuleManager.get(), module, field);
    if (opt.isEmpty()) return true;

    e.setEditable(false);
    if (e instanceof TooltipListEntry<?> t) {
      Optional<Text[]> optional = Optional.of(opt.get().stream()
          .map(text -> {
            if (text instanceof MutableText mt) return mt.formatted(Formatting.RED);
            return text.copy().formatted(Formatting.RED);
          })
          .toArray(Text[]::new));
      t.setTooltipSupplier(() -> optional);
    }
    return false;
  }

  public static <T extends AbstractConfigListEntry<?>> T appendDeprecationInfo(T e, Module module) {
    if (e instanceof TooltipListEntry<?> t) {
      if (!module.getClass().isAnnotationPresent(Deprecated.class)) return e;
      appendText(
          t,
          TextUtil.translatable("andromeda.config.tooltip.deprecated").formatted(Formatting.RED));
    }
    return e;
  }

  public static <T extends AbstractConfigListEntry<?>> T appendEnvInfo(T e, Environment env) {
    if (e instanceof TooltipListEntry<?> t) {
      Text text = TextUtil.translatable(
              "andromeda.config.tooltip.environment." + env.toString().toLowerCase(Locale.ROOT))
          .formatted(Formatting.YELLOW);
      appendText(t, text);
    }
    return e;
  }

  public static <T extends AbstractConfigListEntry<?>> T appendEnvInfo(T e, Field f) {
    if (f.isAnnotationPresent(SpecialEnvironment.class)) {
      SpecialEnvironment env = f.getAnnotation(SpecialEnvironment.class);
      return appendEnvInfo(e, env.value());
    }
    return e;
  }

  public static <T extends TooltipListEntry<?>> T appendText(T t, Text text) {
    var supplier = t.getTooltipSupplier();
    Optional<Text[]> tooltip;
    if (supplier != null) {
      tooltip =
          Optional.of(supplier.get().map(texts -> ArrayUtils.add(texts, text)).orElseGet(() ->
              new Text[] {text}));
    } else {
      tooltip = Optional.of(new Text[] {text});
    }
    t.setTooltipSupplier(() -> tooltip);
    return t;
  }

  public static <T extends AbstractConfigListEntry<?>> T setOptionTooltip(T e, String option) {
    if (e instanceof TooltipListEntry<?> t) {
      if (I18n.hasTranslation(option)) {
        var opt = Optional.of(new Text[] {TextUtil.translatable(option)});
        t.setTooltipSupplier(() -> opt);
        return e;
      }
      t.setTooltipSupplier(Optional::empty);
    }
    return e;
  }

  public static <T extends AbstractConfigListEntry<?>> T wrapTooltip(T e) {
    if (e instanceof TooltipListEntry<?> t) {
      var supplier = t.getTooltipSupplier();
      if (supplier == null) return e;
      var opt = supplier.get().map(texts -> {
        List<Text> wrapped = new ArrayList<>();
        for (Text text : texts) {
          wrapped.addAll(OrderedTextUtil.wrap(text, 250));
        }
        return wrapped.toArray(Text[]::new);
      });
      t.setTooltipSupplier(() -> opt);
    }
    return e;
  }

  public static <T extends AbstractConfigListEntry<?>> T setModuleTooltip(T e, Module module) {
    if (e instanceof TooltipListEntry<?> t) {
      String s = "config.andromeda.%s.@Tooltip".formatted(module.meta().dotted());
      if (!I18n.hasTranslation(s)) return e;

      var opt = Optional.of(new Text[] {TextUtil.translatable(s)});
      t.setTooltipSupplier(() -> opt);
    }
    return e;
  }
}

package dev.zenfyr.andromeda.common.client.config;

import dev.zenfyr.andromeda.bootstrap.Module;
import dev.zenfyr.andromeda.bootstrap.ModuleHelper;
import dev.zenfyr.pulsar.api.util.TextUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import org.apache.commons.lang3.ArrayUtils;

public class ClothTooltipUtil {

  public static <T extends AbstractConfigListEntry<?>> T standardForModule(
      T e, Module module, String field) {
    setModuleTooltip(e, module);
    appendDeprecationTooltip(e, module);
    return wrapTooltip(e);
  }

  public static <T extends AbstractConfigListEntry<?>> T setModuleTooltip(T e, Module module) {
    if (e instanceof TooltipListEntry<?> t) {
      String s = "config.andromeda.%s.@Tooltip".formatted(ModuleHelper.dotted(module));
      if (!Language.getInstance().has(s)) return e;

      var opt = Optional.of(new Component[] {TextUtil.translatable(s)});
      t.setTooltipSupplier(() -> opt);
    }
    return e;
  }

  public static <T extends AbstractConfigListEntry<?>> T setEntryTooltip(T e, String option) {
    if (e instanceof TooltipListEntry<?> t) {
      if (Language.getInstance().has(option)) {
        var opt = Optional.of(new Component[] {TextUtil.translatable(option)});
        t.setTooltipSupplier(() -> opt);
        return e;
      }
      t.setTooltipSupplier(Optional::empty);
    }
    return e;
  }

  public static <T extends AbstractConfigListEntry<?>> T appendDeprecationTooltip(
      T e, Module module) {
    if (e instanceof TooltipListEntry<?> t) {
      if (!module.getClass().isAnnotationPresent(Deprecated.class)) return e;
      appendTooltipText(
          t,
          TextUtil.translatable("andromeda.config.tooltip.deprecated")
              .withStyle(ChatFormatting.RED));
    }
    return e;
  }

  public static <T extends TooltipListEntry<?>> T appendTooltipText(T t, Component text) {
    var supplier = t.getTooltipSupplier();
    Optional<Component[]> tooltip;
    if (supplier != null) {
      tooltip =
          Optional.of(supplier.get().map(texts -> ArrayUtils.add(texts, text)).orElseGet(() ->
              new Component[] {text}));
    } else {
      tooltip = Optional.of(new Component[] {text});
    }
    t.setTooltipSupplier(() -> tooltip);
    return t;
  }

  public static <T extends AbstractConfigListEntry<?>> T wrapTooltip(T e) {
    if (e instanceof TooltipListEntry<?> t) {
      var supplier = t.getTooltipSupplier();
      if (supplier == null) return e;
      var opt = supplier.get().map(texts -> {
        List<Component> wrapped = new ArrayList<>();
        for (Component text : texts) {
          wrapped.addAll(ClothTooltipUtil.wrap(text, 250));
        }
        return wrapped.toArray(Component[]::new);
      });
      t.setTooltipSupplier(() -> opt);
    }
    return e;
  }

  public static List<Component> wrap(Component text, int length) {
    return Minecraft.getInstance().font.split(text, length).stream()
        .map(ClothTooltipUtil::orderedToNormal)
        .toList();
  }

  // author: kyrptonaught
  public static Component orderedToNormal(FormattedCharSequence orderedText) {
    MutableComponent text = TextUtil.empty();
    FormattedCharSink characterVisitor = (index, style, codePoint) -> {
      String str = new String(Character.toChars(codePoint));
      text.append(TextUtil.literal(str).setStyle(style));
      return true;
    };
    orderedText.accept(characterVisitor);
    return text;
  }
}

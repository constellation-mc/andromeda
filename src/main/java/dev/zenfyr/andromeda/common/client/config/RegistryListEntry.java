package dev.zenfyr.andromeda.common.client.config;

import dev.zenfyr.pulsar.util.TextUtil;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.shedaniel.clothconfig2.gui.entries.TextFieldListEntry;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RegistryListEntry<T> extends TextFieldListEntry<T> {

  private final Registry<T> registry;
  private final ResourceLocation defaultKey;

  protected RegistryListEntry(
      Component fieldName,
      Registry<T> registry,
      ResourceLocation defaultKey,
      T original,
      Component resetButtonKey,
      Supplier<T> defaultValue,
      Consumer<T> saveConsumer,
      Supplier<Optional<Component[]>> tooltipSupplier,
      boolean requiresRestart) {
    super(fieldName, original, resetButtonKey, defaultValue, tooltipSupplier, requiresRestart);
    this.registry = registry;
    this.defaultKey = defaultKey;
    this.saveCallback = saveConsumer;

    if (original != null) {
      this.textFieldWidget.setValue(String.valueOf(registry.getKey(original)));
    }

    this.resetButton.onPress = (button) ->
        this.textFieldWidget.setValue(String.valueOf(this.registry.getKey(defaultValue.get())));
  }

  @Override
  protected boolean isChanged(T original, String s) {
    return !Objects.equals(this.registry.getKey(original), tryParse(s));
  }

  @Override
  protected boolean isMatchDefault(String text) {
    if (this.getDefaultValue().isEmpty()) {
      return false;
    }
    return Objects.equals(
        tryParse(text), this.registry.getKey(this.getDefaultValue().get()));
  }

  @Override
  public boolean isEdited() {
    return !Objects.equals(
        this.registry.getKey(original), tryParse(this.textFieldWidget.getValue()));
  }

  @Override
  public Optional<Component> getError() {
    var id = tryParse(this.textFieldWidget.getValue());
    if (id == null)
      return Optional.of(TextUtil.translatable("text.cloth-config.error_cannot_save"));

    if (!this.registry.containsKey(id))
      return Optional.of(TextUtil.translatable("text.cloth-config.error_cannot_save"));

    return super.getError();
  }

  @Override
  public T getValue() {
    var id = tryParse(this.textFieldWidget.getValue());
    if (id != null) {
      T entry = this.registry.get(id);
      if (entry != null) return entry;
    }
    return this.registry.get(this.defaultKey);
  }

  private static ResourceLocation tryParse(String location) {
    if (!ResourceLocation.isValidResourceLocation(location)) return null;
    return ResourceLocation.tryParse(location);
  }
}

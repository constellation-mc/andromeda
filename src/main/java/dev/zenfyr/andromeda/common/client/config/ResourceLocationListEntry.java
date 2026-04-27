package dev.zenfyr.andromeda.common.client.config;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.shedaniel.clothconfig2.gui.entries.TextFieldListEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationListEntry extends TextFieldListEntry<ResourceLocation> {

  protected ResourceLocationListEntry(
      Component fieldName,
      ResourceLocation original,
      Component resetButtonKey,
      Supplier<ResourceLocation> defaultValue,
      Consumer<ResourceLocation> saveConsumer,
      Supplier<Optional<Component[]>> tooltipSupplier,
      boolean requiresRestart) {
    super(fieldName, original, resetButtonKey, defaultValue, tooltipSupplier, requiresRestart);
    this.saveCallback = saveConsumer;
  }

  @Override
  public Optional<Component> getError() {
    var location = ResourceLocation.read(this.textFieldWidget.getValue());
    if (location.error().isPresent())
      return Optional.of(Component.literal(location.error().orElseThrow().message()));
    return super.getError();
  }

  @Override
  public ResourceLocation getValue() {
    var id = ResourceLocation.tryParse(this.textFieldWidget.getValue());
    if (id != null) return id;
    return ResourceLocation.withDefaultNamespace("");
  }
}

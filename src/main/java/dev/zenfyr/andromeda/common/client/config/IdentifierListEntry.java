package dev.zenfyr.andromeda.common.client.config;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import me.shedaniel.clothconfig2.gui.entries.TextFieldListEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class IdentifierListEntry extends TextFieldListEntry<Identifier> {

  protected IdentifierListEntry(
      Component fieldName,
      Identifier original,
      Component resetButtonKey,
      Supplier<Identifier> defaultValue,
      Consumer<Identifier> saveConsumer,
      Supplier<Optional<Component[]>> tooltipSupplier,
      boolean requiresRestart) {
    super(fieldName, original, resetButtonKey, defaultValue, tooltipSupplier, requiresRestart);
    this.saveCallback = saveConsumer;
  }

  @Override
  public Optional<Component> getError() {
    var location = Identifier.read(this.textFieldWidget.getValue());
    if (location.error().isPresent())
      return Optional.of(Component.literal(location.error().orElseThrow().message()));
    return super.getError();
  }

  @Override
  public Identifier getValue() {
    var id = Identifier.tryParse(this.textFieldWidget.getValue());
    if (id != null) return id;
    return Identifier.withDefaultNamespace("");
  }
}

package dev.zenfyr.andromeda.bootstrap.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public abstract class BaseConfig {

  private static final Map<Class<? extends BaseConfig>, Set<Field>> CLASS_DATA =
      new IdentityHashMap<>();

  private final transient Set<Field> classData;

  public BaseConfig() {
    this.classData = CLASS_DATA.computeIfAbsent(this.getClass(), aClass -> {
      Set<Field> fields = new LinkedHashSet<>();
      for (Field field : this.getClass().getFields()) {
        if (Modifier.isStatic(field.getModifiers())) continue;
        if (Modifier.isTransient(field.getModifiers())) continue;
        if (Modifier.isFinal(field.getModifiers()))
          // We use no-arg constructors, so final fields would be impossible to use in copy()
          throw new IllegalStateException("All config fields must not be final!");
        fields.add(field);
      }
      return fields;
    });
  }

  public BaseConfig copy() {
    try {
      BaseConfig instance = (BaseConfig) this.getClass().getConstructors()[0].newInstance();
      for (Field field : this.classData) {
        field.set(instance, field.get(this));
      }
      return instance;
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof BaseConfig bc)) return false;
    if (this.getClass() != obj.getClass()) return false;

    try {
      for (Field field : this.classData) {
        if (!Objects.equals(field.get(bc), field.get(this))) return false;
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.classData.stream()
        .map(field -> {
          try {
            return field.get(this);
          } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        })
        .toArray(Object[]::new));
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(
        this.getClass().getName().substring(this.getClass().getPackageName().length() + 1));
    StringJoiner joiner = new StringJoiner(", ", "(", ")");
    try {
      for (Field field : this.classData) {
        joiner.add(Objects.toString(field.get(this)));
      }
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    return builder.append(joiner).toString();
  }
}

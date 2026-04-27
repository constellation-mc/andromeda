package dev.zenfyr.andromeda.modules.entities.boats;

import dev.zenfyr.andromeda.common.Andromeda;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class BoatTypes {

  private static final List<BoatType> BOAT_TYPES = new ArrayList<>();
  private static final List<BoatVariant> BOAT_VARIANTS = new ArrayList<>();

  public static final BoatVariant FURNACE = BoatVariant.of("furnace");
  public static final BoatVariant HOPPER = BoatVariant.of("hopper");
  public static final BoatVariant JUKEBOX = BoatVariant.of("jukebox");
  public static final BoatVariant TNT = BoatVariant.of("tnt");

  public static final BoatModel BOAT = BoatModel.of("boat");
  public static final BoatModel RAFT = BoatModel.of("raft");

  public static final BoatType OAK = BoatType.of("oak", BOAT);
  public static final BoatType SPRUCE = BoatType.of("spruce", BOAT);
  public static final BoatType BIRCH = BoatType.of("birch", BOAT);
  public static final BoatType JUNGLE = BoatType.of("jungle", BOAT);
  public static final BoatType ACACIA = BoatType.of("acacia", BOAT);
  public static final BoatType DARK_OAK = BoatType.of("dark_oak", BOAT);
  public static final BoatType MANGROVE = BoatType.of("mangrove", BOAT);
  public static final BoatType CHERRY = BoatType.of("cherry", BOAT);
  public static final BoatType PALE_OAK = BoatType.of("pale_oak", BOAT);
  public static final BoatType BAMBOO = BoatType.of("bamboo", RAFT);

  public static ResourceLocation location(BoatType type, BoatVariant variant) {
    return Andromeda.id(type.material() + '_' + variant.name() + "_boat");
  }

  public static <T> ResourceKey<T> key(
      ResourceKey<? extends Registry<T>> registry, BoatType type, BoatVariant variant) {
    return Andromeda.key(registry, type.material() + '_' + variant.name() + "_boat");
  }

  public static List<BoatVariant> getBoatVariants() {
    return BOAT_VARIANTS;
  }

  public static List<BoatType> getBoatTypes() {
    return BOAT_TYPES;
  }

  public record BoatModel(String name) {
    public static BoatModel of(String name) {
      return new BoatModel(name);
    }
  }

  public record BoatVariant(String name) {
    public static BoatVariant of(String name) {
      var variant = new BoatVariant(name);
      BOAT_VARIANTS.add(variant);
      return variant;
    }
  }

  public record BoatType(String material, BoatModel model) {
    public static BoatType of(String material, BoatModel model) {
      var type = new BoatType(material, model);
      BOAT_TYPES.add(type);
      return type;
    }
  }
}

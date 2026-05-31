package dev.zenfyr.andromeda.modules.entities.boats;

import dev.zenfyr.andromeda.common.Andromeda;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector2f;

public class BoatTypes {

  private static final List<BoatType> BOAT_TYPES = new ArrayList<>();
  private static final List<BoatVariant> BOAT_VARIANTS = new ArrayList<>();

  public static final BoatVariant FURNACE =
      BoatVariant.of("furnace", Blocks.FURNACE.defaultBlockState());
  public static final BoatVariant HOPPER =
      BoatVariant.of("hopper", Blocks.HOPPER.defaultBlockState());
  public static final BoatVariant JUKEBOX =
      BoatVariant.of("jukebox", Blocks.JUKEBOX.defaultBlockState());
  public static final BoatVariant TNT = BoatVariant.of("tnt", Blocks.TNT.defaultBlockState());

  public static final BoatModel BOAT = BoatModel.of("boat", new Vector2f(0.25f, 1.0f), 0.333333333);
  public static final BoatModel RAFT = BoatModel.of("raft", new Vector2f(0.635f, 1.09f), 0.8888889);

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
    return Andromeda.id(
        type.material() + '_' + variant.name() + '_' + type.model().name());
  }

  public static <T> ResourceKey<T> key(
      ResourceKey<? extends Registry<T>> registry, BoatType type, BoatVariant variant) {
    return Andromeda.key(
        registry, type.material() + '_' + variant.name() + '_' + type.model().name());
  }

  public static List<BoatVariant> getBoatVariants() {
    return BOAT_VARIANTS;
  }

  public static List<BoatType> getBoatTypes() {
    return BOAT_TYPES;
  }

  public record BoatModel(String name, Vector2f offset, double rideHeight) {
    public static BoatModel of(String name, Vector2f offset, double rideHeight) {
      return new BoatModel(name, offset, rideHeight);
    }
  }

  public record BoatVariant(String name, BlockState state) {
    public static BoatVariant of(String name, BlockState state) {
      var variant = new BoatVariant(name, state);
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

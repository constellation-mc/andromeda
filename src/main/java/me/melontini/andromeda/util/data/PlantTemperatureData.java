package me.melontini.andromeda.util.data;

import net.minecraft.block.Block;

public record PlantTemperatureData(Block block, float min, float max, float aMin, float aMax) {

    @Override
    public String toString() {
        return "PlantTemperatureData{" +
                "block=" + block +
                ", min=" + min +
                ", max=" + max +
                ", aMin=" + aMin +
                ", aMax=" + aMax +
                '}';
    }

}

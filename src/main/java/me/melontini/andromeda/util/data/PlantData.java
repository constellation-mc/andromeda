package me.melontini.andromeda.util.data;

public class PlantData {
    public String identifier;
    public float min;
    public float max;
    public float aMin;
    public float aMax;

    PlantData(String identifier, float min, float max, float aMin, float aMax) {
        this.identifier = identifier;
        this.min = min;
        this.max = max;
        this.aMin = aMin;
        this.aMax = aMax;
    }
}

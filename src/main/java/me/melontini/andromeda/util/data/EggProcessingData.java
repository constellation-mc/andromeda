package me.melontini.andromeda.util.data;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;

public record EggProcessingData(Item item, EntityType<?> entity, int time) {

}

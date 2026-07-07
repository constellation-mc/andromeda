package dev.zenfyr.andromeda.modules.items.magnet;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.component.BundleContents;

public record MagnetTooltip(BundleContents contents) implements TooltipComponent {}

package com.powergrid.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PowerGridBlockItem extends BlockItem {
    private final Supplier<List<Component>> tooltipSupplier;

    public PowerGridBlockItem(Block block, Properties properties, Supplier<List<Component>> tooltipSupplier) {
        super(block, properties.useBlockDescriptionPrefix());
        this.tooltipSupplier = tooltipSupplier;
    }

    public PowerGridBlockItem(Block block, Properties properties, List<Component> staticTooltips) {
        this(block, properties, () -> staticTooltips);
    }

    public PowerGridBlockItem(Block block, Properties properties, Component... tooltips) {
        this(block, properties, () -> List.of(tooltips));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        if (tooltipSupplier != null) {
            List<Component> lines = tooltipSupplier.get();
            if (lines != null) {
                for (Component line : lines) {
                    builder.accept(line);
                }
            }
        }
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}

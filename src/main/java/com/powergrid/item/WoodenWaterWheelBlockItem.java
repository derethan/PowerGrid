package com.powergrid.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class WoodenWaterWheelBlockItem extends BlockItem {
    public WoodenWaterWheelBlockItem(Block block, Properties properties) {
        super(block, properties.useBlockDescriptionPrefix());
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltip.powergrid.wooden_water_wheel.desc"));
        builder.accept(Component.translatable("tooltip.powergrid.wooden_water_wheel.stack"));
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}

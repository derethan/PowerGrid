package com.powergrid.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class WindmillRotorBlockItem extends BlockItem {
    public WindmillRotorBlockItem(Block block, Properties properties) {
        super(block, properties.useBlockDescriptionPrefix());
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        builder.accept(Component.translatable("tooltip.powergrid.windmill_rotor.desc"));
        builder.accept(Component.translatable("tooltip.powergrid.windmill_rotor.clearance"));
        builder.accept(Component.translatable("tooltip.powergrid.windmill_rotor.stack"));
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}

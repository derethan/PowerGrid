package com.powergrid.item;

import com.powergrid.block.PowerCableBlock;
import com.powergrid.grid.CableGauge;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class PowerCableBlockItem extends BlockItem {
    public PowerCableBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        if (this.getBlock() instanceof PowerCableBlock pcb) {
            CableGauge gauge = pcb.getGauge();
            builder.accept(Component.translatable("tooltip.powergrid.cable_gauge", gauge.getDisplayName()));
            builder.accept(Component.translatable("tooltip.powergrid.cable_rate", String.format("%,d", gauge.getTransferRate())));
            builder.accept(Component.translatable("tooltip.powergrid.cable_buffer", String.format("%,d", gauge.getBufferCapacity())));
        }
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}

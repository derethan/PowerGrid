package com.powergrid.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class MaterialItem extends Item {
    @Nullable
    private final String tooltipKey;

    public MaterialItem(Properties properties, @Nullable String tooltipKey) {
        super(properties);
        this.tooltipKey = tooltipKey;
    }

    public MaterialItem(Properties properties) {
        this(properties, null);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        if (this.tooltipKey != null) {
            builder.accept(Component.translatable(this.tooltipKey));
        }
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);
    }
}

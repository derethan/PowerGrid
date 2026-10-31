package com.powergrid.grid;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum ConnectorMode implements StringRepresentable {
    AUTO("auto", "Auto-Sensing", ChatFormatting.GREEN),
    INPUT("input", "Input (From Machine)", ChatFormatting.GOLD),
    OUTPUT("output", "Output (To Machine)", ChatFormatting.AQUA);

    private final String name;
    private final String description;
    private final ChatFormatting color;

    ConnectorMode(String name, String description, ChatFormatting color) {
        this.name = name;
        this.description = description;
        this.color = color;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public String getDescription() {
        return description;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public Component getFormattedName() {
        return Component.literal(description).withStyle(color);
    }

    public ConnectorMode next() {
        return switch (this) {
            case AUTO -> INPUT;
            case INPUT -> OUTPUT;
            case OUTPUT -> AUTO;
        };
    }
}

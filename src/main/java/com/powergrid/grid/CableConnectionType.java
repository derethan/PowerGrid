package com.powergrid.grid;

import net.minecraft.util.StringRepresentable;

public enum CableConnectionType implements StringRepresentable {
    NONE("none"),
    CABLE("cable"),
    DEVICE("device");

    private final String name;

    CableConnectionType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public boolean isConnected() {
        return this != NONE;
    }
}

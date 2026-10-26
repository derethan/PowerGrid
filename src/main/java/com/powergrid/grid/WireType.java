package com.powergrid.grid;

import net.minecraft.util.StringRepresentable;

public enum WireType implements StringRepresentable {
    LV("lv", 400, 16, 0.02f, "Low Voltage (LV)"),
    MV("mv", 2048, 32, 0.01f, "Medium Voltage (MV)"),
    HV("hv", 16384, 64, 0.005f, "High Voltage (HV)");

    private final String name;
    private final int transferRate; // FE/t max capacity
    private final int maxDistance;  // Max distance between connectors in blocks
    private final float lossPerBlock; // Energy loss percentage per block
    private final String displayName;

    WireType(String name, int transferRate, int maxDistance, float lossPerBlock, String displayName) {
        this.name = name;
        this.transferRate = transferRate;
        this.maxDistance = maxDistance;
        this.lossPerBlock = lossPerBlock;
        this.displayName = displayName;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public int getTransferRate() {
        return transferRate;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public float getLossPerBlock() {
        return lossPerBlock;
    }

    public String getDisplayName() {
        return displayName;
    }
}

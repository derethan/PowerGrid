package com.powergrid.grid;

import com.powergrid.PowerGridMod;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;

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

    public Item getCoilItem() {
        return switch (this) {
            case LV -> PowerGridMod.LV_WIRE_COIL.get();
            case MV -> PowerGridMod.MV_WIRE_COIL.get();
            case HV -> PowerGridMod.HV_WIRE_COIL.get();
        };
    }

    public int getColor() {
        return switch (this) {
            case LV -> 0xFFB85D19; // Copper reddish brown
            case MV -> 0xFF4A4E5A; // Dark insulated slate
            case HV -> 0xFFC5CBD3; // Galvanized aluminum-steel strand
        };
    }

    public float getCableRadius() {
        return switch (this) {
            case LV -> 0.025f;
            case MV -> 0.035f;
            case HV -> 0.045f;
        };
    }

    public float getSagFactor() {
        return switch (this) {
            case LV -> 0.035f;
            case MV -> 0.030f;
            case HV -> 0.022f;
        };
    }
}

package com.powergrid.grid;

import com.powergrid.PowerGridMod;
import com.powergrid.config.PowerGridConfig;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;

public enum WireType implements StringRepresentable {
    LV("lv", 400, 16, 0.02f, "Low Voltage (LV)"),
    MV("mv", 2048, 32, 0.01f, "Medium Voltage (MV)"),
    HV("hv", 16384, 64, 0.005f, "High Voltage (HV)");

    private final String name;
    private final int transferRate; // FE/t max capacity
    private final int maxDistance;  // Max distance between connectors in blocks
    private final float defaultLossPerBlock; // Intended production energy loss percentage per block
    private final String displayName;

    WireType(String name, int transferRate, int maxDistance, float defaultLossPerBlock, String displayName) {
        this.name = name;
        this.transferRate = transferRate;
        this.maxDistance = maxDistance;
        this.defaultLossPerBlock = defaultLossPerBlock;
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
        if (PowerGridConfig.SPEC.isLoaded() && PowerGridConfig.CONFIG != null) {
            return switch (this) {
                case LV -> PowerGridConfig.CONFIG.lvLossPerBlock.get().floatValue();
                case MV -> PowerGridConfig.CONFIG.mvLossPerBlock.get().floatValue();
                case HV -> PowerGridConfig.CONFIG.hvLossPerBlock.get().floatValue();
            };
        }
        return 0.0f;
    }

    public float getDefaultLossPerBlock() {
        return defaultLossPerBlock;
    }

    public int getLossFreeDistance() {
        if (PowerGridConfig.SPEC.isLoaded() && PowerGridConfig.CONFIG != null) {
            return switch (this) {
                case LV -> PowerGridConfig.CONFIG.lvLossFreeDistance.get();
                case MV -> PowerGridConfig.CONFIG.mvLossFreeDistance.get();
                case HV -> PowerGridConfig.CONFIG.hvLossFreeDistance.get();
            };
        }
        return getDefaultLossFreeDistance();
    }

    public int getDefaultLossFreeDistance() {
        return switch (this) {
            case LV -> 8;
            case MV -> 16;
            case HV -> 24;
        };
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

    public static WireType fromString(String name) {
        if (name == null) return LV;
        if ("hv".equalsIgnoreCase(name)) return HV;
        if ("mv".equalsIgnoreCase(name)) return MV;
        return LV;
    }
}

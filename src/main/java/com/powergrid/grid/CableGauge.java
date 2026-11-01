package com.powergrid.grid;

import net.minecraft.util.StringRepresentable;

public enum CableGauge implements StringRepresentable {
    GAUGE_18("18_gauge", 18, 128, "18 AWG (Copper)"),
    GAUGE_16("16_gauge", 16, 512, "16 AWG (Iron)"),
    GAUGE_14("14_gauge", 14, 2048, "14 AWG (Steel)"),
    GAUGE_12("12_gauge", 12, 8192, "12 AWG (Gold)");

    private final String name;
    private final int awg;
    private final int transferRate;
    private final String displayName;

    CableGauge(String name, int awg, int transferRate, String displayName) {
        this.name = name;
        this.awg = awg;
        this.transferRate = transferRate;
        this.displayName = displayName;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public int getAwg() {
        return awg;
    }

    public int getTransferRate() {
        return transferRate;
    }

    public int getBufferCapacity() {
        return transferRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CableGauge fromString(String name) {
        if (name == null) return GAUGE_18;
        for (CableGauge g : values()) {
            if (g.name.equalsIgnoreCase(name) || g.name().equalsIgnoreCase(name)) {
                return g;
            }
        }
        return GAUGE_18;
    }
}

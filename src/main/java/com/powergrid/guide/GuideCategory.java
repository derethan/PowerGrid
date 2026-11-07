package com.powergrid.guide;

public enum GuideCategory {
    ALL("All Contents", 0xFFE2E8F0),
    GENERATION("Power Generation", 0xFFF59E0B),
    TRANSMISSION("Overhead Grid", 0xFF38BDF8),
    SUBSTATION("Substation & Storage", 0xFFEF4444),
    DISTRIBUTION("Indoor Wiring", 0xFF10B981),
    LIGHTING("Illumination Suite", 0xFFFCD34D),
    TOOLS("Tools & Materials", 0xFFA855F7);

    private final String displayName;
    private final int color;

    GuideCategory(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }
}

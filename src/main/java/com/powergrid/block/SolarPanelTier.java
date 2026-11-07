package com.powergrid.block;

import com.powergrid.PowerGridMod;
import com.powergrid.config.PowerGridConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

public enum SolarPanelTier implements StringRepresentable {
    STANDARD("standard", 1, 10_000, 100, "solar_panel_monolith.png"),
    GOLD("gold", 2, 20_000, 200, "solar_panel_monolith_gold.png"),
    DIAMOND("diamond", 5, 50_000, 500, "solar_panel_monolith_diamond.png");

    private final String name;
    private final int defaultFePerTick;
    private final int defaultCapacity;
    private final int defaultMaxExport;
    private final Identifier texture;

    SolarPanelTier(String name, int defaultFePerTick, int defaultCapacity, int defaultMaxExport, String textureFile) {
        this.name = name;
        this.defaultFePerTick = defaultFePerTick;
        this.defaultCapacity = defaultCapacity;
        this.defaultMaxExport = defaultMaxExport;
        this.texture = Identifier.fromNamespaceAndPath(PowerGridMod.MODID, "textures/entity/" + textureFile);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public int getGenerationRate() {
        try {
            if (PowerGridConfig.CONFIG != null && PowerGridConfig.CONFIG.solarPanelBaseFePerTick != null) {
                return switch (this) {
                    case STANDARD -> PowerGridConfig.CONFIG.solarPanelBaseFePerTick.get();
                    case GOLD -> PowerGridConfig.CONFIG.solarPanelGoldFePerTick.get();
                    case DIAMOND -> PowerGridConfig.CONFIG.solarPanelDiamondFePerTick.get();
                };
            }
        } catch (Exception ignored) {}
        return this.defaultFePerTick;
    }

    public int getCapacity() {
        try {
            if (PowerGridConfig.CONFIG != null && PowerGridConfig.CONFIG.solarPanelBaseCapacity != null) {
                return switch (this) {
                    case STANDARD -> PowerGridConfig.CONFIG.solarPanelBaseCapacity.get();
                    case GOLD -> PowerGridConfig.CONFIG.solarPanelGoldCapacity.get();
                    case DIAMOND -> PowerGridConfig.CONFIG.solarPanelDiamondCapacity.get();
                };
            }
        } catch (Exception ignored) {}
        return this.defaultCapacity;
    }

    public int getMaxExportRate() {
        try {
            if (PowerGridConfig.CONFIG != null && PowerGridConfig.CONFIG.solarPanelBaseMaxExport != null) {
                return switch (this) {
                    case STANDARD -> PowerGridConfig.CONFIG.solarPanelBaseMaxExport.get();
                    case GOLD -> PowerGridConfig.CONFIG.solarPanelGoldMaxExport.get();
                    case DIAMOND -> PowerGridConfig.CONFIG.solarPanelDiamondMaxExport.get();
                };
            }
        } catch (Exception ignored) {}
        return this.defaultMaxExport;
    }

    public Identifier getTexture() {
        return this.texture;
    }

    public String getDisplayName() {
        return switch (this) {
            case STANDARD -> "Standard Solar Panel";
            case GOLD -> "Gold Solar Panel (2x)";
            case DIAMOND -> "Diamond Solar Panel (5x)";
        };
    }
}

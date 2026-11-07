package com.powergrid.block;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SolarPanelTierTest {

    @Test
    public void testTierMultipliers() {
        SolarPanelTier standard = SolarPanelTier.STANDARD;
        SolarPanelTier gold = SolarPanelTier.GOLD;
        SolarPanelTier diamond = SolarPanelTier.DIAMOND;

        // Baseline: Standard = 1 FE/t
        assertEquals(1, standard.getGenerationRate(), "Standard solar panel should generate 1 FE/t");

        // Gold: 2x baseline = 2 FE/t
        assertEquals(2, gold.getGenerationRate(), "Gold solar panel should generate 2 FE/t (2x)");
        assertEquals(standard.getGenerationRate() * 2, gold.getGenerationRate(), "Gold tier must be exactly 2x standard");

        // Diamond: 5x baseline = 5 FE/t
        assertEquals(5, diamond.getGenerationRate(), "Diamond solar panel should generate 5 FE/t (5x)");
        assertEquals(standard.getGenerationRate() * 5, diamond.getGenerationRate(), "Diamond tier must be exactly 5x standard");
    }

    @Test
    public void testCapacitiesAndExportRates() {
        SolarPanelTier standard = SolarPanelTier.STANDARD;
        SolarPanelTier gold = SolarPanelTier.GOLD;
        SolarPanelTier diamond = SolarPanelTier.DIAMOND;

        assertTrue(standard.getCapacity() > 0);
        assertTrue(gold.getCapacity() >= standard.getCapacity() * 2);
        assertTrue(diamond.getCapacity() >= standard.getCapacity() * 5);

        assertTrue(standard.getMaxExportRate() > 0);
        assertTrue(gold.getMaxExportRate() >= standard.getMaxExportRate() * 2);
        assertTrue(diamond.getMaxExportRate() >= standard.getMaxExportRate() * 5);
    }

    @Test
    public void testTextureIdentifiers() {
        for (SolarPanelTier tier : SolarPanelTier.values()) {
            assertNotNull(tier.getTexture());
            assertEquals("powergrid", tier.getTexture().getNamespace());
            assertTrue(tier.getTexture().getPath().startsWith("textures/entity/solar_panel_monolith"));
        }
    }
}

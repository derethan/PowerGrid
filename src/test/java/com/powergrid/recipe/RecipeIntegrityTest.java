package com.powergrid.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.powergrid.guide.GuideBookRegistry;
import com.powergrid.guide.GuideCategory;
import com.powergrid.guide.GuideEntry;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileReader;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RecipeIntegrityTest {

    private static final String RECIPE_DIR = "src/main/resources/data/powergrid/recipe";

    // All items registered in PowerGridMod that should be craftable in survival
    private static final Set<String> SURVIVAL_ITEMS = new HashSet<>(Arrays.asList(
            "powergrid:treated_wood_pole",
            "powergrid:steel_truss_pole",
            "powergrid:pole_crossarm",
            "powergrid:lv_wire_connector",
            "powergrid:mv_wire_connector",
            "powergrid:hv_wire_connector",
            "powergrid:lv_wire_relay",
            "powergrid:mv_wire_relay",
            "powergrid:hv_wire_relay",
            "powergrid:cable_18_gauge",
            "powergrid:cable_16_gauge",
            "powergrid:cable_14_gauge",
            "powergrid:cable_12_gauge",
            "powergrid:transformer",
            "powergrid:breaker_switch",
            "powergrid:heavy_accumulator",
            "powergrid:wooden_water_wheel",
            "powergrid:windmill_rotor",
            "powergrid:solar_panel",
            "powergrid:gold_solar_panel",
            "powergrid:diamond_solar_panel",
            "powergrid:tripod_work_light",
            "powergrid:ceiling_shop_light",
            "powergrid:wall_floodlight",
            "powergrid:carriage_lantern",
            "powergrid:street_light",
            "powergrid:engineer_hammer",
            "powergrid:voltmeter",
            "powergrid:wire_cutters",
            "powergrid:lv_wire_coil",
            "powergrid:mv_wire_coil",
            "powergrid:hv_wire_coil",
            "powergrid:treated_wood",
            "powergrid:copper_wire",
            "powergrid:electrum_wire",
            "powergrid:steel_strand",
            "powergrid:ceramic_insulator",
            "powergrid:guide_book"
    ));

    @Test
    void testAllSurvivalItemsHaveRecipes() {
        File dir = new File(RECIPE_DIR);
        assertTrue(dir.exists() && dir.isDirectory(), "Recipe directory must exist: " + RECIPE_DIR);

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        assertNotNull(files, "Recipe files must not be null");
        assertTrue(files.length > 0, "There should be recipe files");

        Set<String> itemsWithRecipes = new HashSet<>();

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonElement root = JsonParser.parseReader(reader);
                assertTrue(root.isJsonObject(), file.getName() + " must be a JSON object");
                JsonObject obj = root.getAsJsonObject();

                assertTrue(obj.has("result"), file.getName() + " must have a result property");
                JsonElement resultElem = obj.get("result");

                String resultId;
                if (resultElem.isJsonObject()) {
                    JsonObject resultObj = resultElem.getAsJsonObject();
                    assertTrue(resultObj.has("id"), file.getName() + " result object must have 'id'");
                    resultId = resultObj.get("id").getAsString();
                } else if (resultElem.isJsonPrimitive()) {
                    resultId = resultElem.getAsString();
                } else {
                    fail("Unexpected result format in " + file.getName());
                    return;
                }

                itemsWithRecipes.add(resultId);
            } catch (Exception e) {
                fail("Failed to parse " + file.getName() + ": " + e.getMessage());
            }
        }

        Set<String> missingRecipes = new HashSet<>(SURVIVAL_ITEMS);
        missingRecipes.removeAll(itemsWithRecipes);

        assertTrue(missingRecipes.isEmpty(),
                "The following survival items/blocks have NO recipe: " + missingRecipes);
    }

    @Test
    void testTreatedWoodAndWaterWheelAndWindmillRecipes() throws Exception {
        // Test treated wood recipe exists and produces 8
        File twFile = new File(RECIPE_DIR, "treated_wood.json");
        assertTrue(twFile.exists(), "treated_wood.json must exist");
        try (FileReader reader = new FileReader(twFile)) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            assertEquals("powergrid:treated_wood", obj.getAsJsonObject("result").get("id").getAsString());
            assertEquals(8, obj.getAsJsonObject("result").get("count").getAsInt());
        }

        // Test water wheel recipe uses treated wood
        File wwFile = new File(RECIPE_DIR, "wooden_water_wheel.json");
        assertTrue(wwFile.exists(), "wooden_water_wheel.json must exist");
        try (FileReader reader = new FileReader(wwFile)) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject key = obj.getAsJsonObject("key");
            assertTrue(key.has("W"), "Water wheel recipe must have 'W' key for treated wood");
            String wValue = key.get("W").getAsString();
            assertTrue(wValue.contains("treated_wood"), "Water wheel 'W' must refer to treated_wood");
        }

        // Test windmill rotor recipe uses treated wood
        File wrFile = new File(RECIPE_DIR, "windmill_rotor.json");
        assertTrue(wrFile.exists(), "windmill_rotor.json must exist");
        try (FileReader reader = new FileReader(wrFile)) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject key = obj.getAsJsonObject("key");
            assertTrue(key.has("W"), "Windmill rotor recipe must have 'W' key for treated wood");
            String wValue = key.get("W").getAsString();
            assertTrue(wValue.contains("treated_wood"), "Windmill rotor 'W' must refer to treated_wood");
        }
    }

    @Test
    void testGuideBookRegistryEntries() {
        List<GuideEntry> allEntries = GuideBookRegistry.getAllEntries();
        assertFalse(allEntries.isEmpty(), "Guide entries should not be empty");
        assertTrue(allEntries.size() >= 10, "Should have comprehensive guide entries");

        for (GuideCategory cat : GuideCategory.values()) {
            List<GuideEntry> catEntries = GuideBookRegistry.getEntriesByCategory(cat);
            assertNotNull(catEntries);
        }

        assertNotNull(GuideBookRegistry.getEntry("water_wheel"));
        assertNotNull(GuideBookRegistry.getEntry("windmill_rotor"));
        assertNotNull(GuideBookRegistry.getEntry("solar_panels"));
        assertNotNull(GuideBookRegistry.getEntry("voltage_tiers"));
        assertNotNull(GuideBookRegistry.getEntry("poles_and_towers"));
        assertNotNull(GuideBookRegistry.getEntry("connectors_and_relays"));
        assertNotNull(GuideBookRegistry.getEntry("transformer"));
        assertNotNull(GuideBookRegistry.getEntry("heavy_accumulator"));
        assertNotNull(GuideBookRegistry.getEntry("breaker_switch"));
        assertNotNull(GuideBookRegistry.getEntry("awg_cables"));
        assertNotNull(GuideBookRegistry.getEntry("lighting_suite"));
        assertNotNull(GuideBookRegistry.getEntry("engineering_tools"));
        assertNotNull(GuideBookRegistry.getEntry("materials_crafting"));
    }
}

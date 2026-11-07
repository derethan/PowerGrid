package com.powergrid.guide;

import com.powergrid.PowerGridMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GuideBookRegistry {
    private static final List<GuideEntry> ENTRIES = new ArrayList<>();
    private static final Map<String, GuideEntry> BY_ID = new HashMap<>();
    private static boolean initialized = false;

    private static ItemStack mcItem(String name) {
        return mcItem(name, 1);
    }

    private static ItemStack mcItem(String name, int count) {
        Identifier id = name.contains(":") ? Identifier.parse(name) : Identifier.fromNamespaceAndPath("minecraft", name);
        Item item = BuiltInRegistries.ITEM.getValue(id);
        return item != null ? new ItemStack(item, count) : ItemStack.EMPTY;
    }

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        // =========================================================================
        // 1. WOODEN WATER WHEEL (3x3x1 Multiblock)
        // =========================================================================
        {
            Map<Character, LayerSymbol> legend = new HashMap<>();
            legend.put('C', new LayerSymbol('C', "Center Axle Hub (Power Export)", 0xFFD97706, () -> new ItemStack(PowerGridMod.WOODEN_WATER_WHEEL_ITEM.get())));
            legend.put('T', new LayerSymbol('T', "Top Water Blade", 0xFF92400E, () -> new ItemStack(PowerGridMod.TREATED_WOOD.get())));
            legend.put('B', new LayerSymbol('B', "Bottom Water Blade", 0xFF92400E, () -> new ItemStack(PowerGridMod.TREATED_WOOD.get())));
            legend.put('L', new LayerSymbol('L', "Left Outer Paddle", 0xFF78350F, () -> new ItemStack(PowerGridMod.TREATED_WOOD.get())));
            legend.put('R', new LayerSymbol('R', "Right Outer Paddle", 0xFF78350F, () -> new ItemStack(PowerGridMod.TREATED_WOOD.get())));

            List<GuideBlockRequirement> reqs = List.of(
                    new GuideBlockRequirement(() -> new ItemStack(PowerGridMod.WOODEN_WATER_WHEEL_ITEM.get()), "Wooden Water Wheel", 1)
            );

            List<GuideLayer> layers = List.of(
                    new GuideLayer(0, "Vertical Face (3x3 Multiblock)", new String[]{
                            "LTR",
                            "LCR",
                            "LBR"
                    }, legend)
            );

            List<GuideRecipe> recipes = List.of(
                    new GuideRecipe("Wooden Water Wheel",
                            List.of(
                                    () -> new ItemStack(PowerGridMod.TREATED_WOOD.get(), 4),
                                    () -> mcItem("oak_planks", 4),
                                    () -> mcItem("iron_ingot", 1)
                            ),
                            List.of(() -> new ItemStack(PowerGridMod.WOODEN_WATER_WHEEL_ITEM.get())),
                            "WTW / TIT / WTW in Crafting Table")
            );

            register(new GuideEntry(
                    "water_wheel",
                    "Wooden Water Wheel",
                    "Kinetic Hydroelectric Multiblock (3x3x1)",
                    GuideCategory.GENERATION,
                    () -> new ItemStack(PowerGridMod.WOODEN_WATER_WHEEL_ITEM.get()),
                    "Large 3-block diameter, 1-block wide timber kinetic water wheel harvesting renewable rotational torque from moving water currents.\n\n" +
                    "Water Drop Head Scaling:\n" +
                    "Generates 10 FE/tick per vertical height level of flowing water drop (e.g. 1-block drop = 10 FE/t, 2-block = 20 FE/t, up to 50 FE/t for overhead waterfall plunges).\n\n" +
                    "Flowing Water Requirement:\n" +
                    "Must be placed in flowing water currents or waterfalls; static standing source water produces zero torque/FE.\n\n" +
                    "Axle Stacking:\n" +
                    "Water wheels placed side-by-side along their axle axis automatically form a unified drive shaft line, pooling and transferring energy cleanly across the stack to terminal wheels.",
                    "Stack multiple water wheels side-by-side along their axle shaft! Only the terminal wheel needs to connect to an accumulator, cable, or transformer.",
                    reqs,
                    layers,
                    recipes
            ));
        }

        // =========================================================================
        // 2. WINDMILL ROTOR (3x3x1 Multiblock)
        // =========================================================================
        {
            Map<Character, LayerSymbol> legend = new HashMap<>();
            legend.put('C', new LayerSymbol('C', "Banded Hub & Bearing (Power Output)", 0xFFD97706, () -> new ItemStack(PowerGridMod.WINDMILL_ROTOR_ITEM.get())));
            legend.put('T', new LayerSymbol('T', "Heavy Timber Spar", 0xFF78350F, () -> new ItemStack(PowerGridMod.TREATED_WOOD.get())));
            legend.put('S', new LayerSymbol('S', "Aerodynamic Canvas Sail Slat", 0xFFF1F5F9, () -> mcItem("white_wool")));

            List<GuideBlockRequirement> reqs = List.of(
                    new GuideBlockRequirement(() -> new ItemStack(PowerGridMod.WINDMILL_ROTOR_ITEM.get()), "Windmill Rotor", 1)
            );

            List<GuideLayer> layers = List.of(
                    new GuideLayer(0, "Rotor Face (3x3 Multiblock)", new String[]{
                            "STS",
                            "TCT",
                            "STS"
                    }, legend)
            );

            List<GuideRecipe> recipes = List.of(
                    new GuideRecipe("Windmill Rotor",
                            List.of(
                                    () -> mcItem("white_wool", 4),
                                    () -> new ItemStack(PowerGridMod.TREATED_WOOD.get(), 4),
                                    () -> mcItem("iron_ingot", 1)
                            ),
                            List.of(() -> new ItemStack(PowerGridMod.WINDMILL_ROTOR_ITEM.get())),
                            "CWC / WIW / CWC in Crafting Table")
            );

            register(new GuideEntry(
                    "windmill_rotor",
                    "Windmill Rotor",
                    "Aerodynamic Kinetic Wind Turbine (3x3x1)",
                    GuideCategory.GENERATION,
                    () -> new ItemStack(PowerGridMod.WINDMILL_ROTOR_ITEM.get()),
                    "Authentic 3-block diameter, 1-block wide industrial wind turbine rotor harvesting kinetic energy from open atmospheric wind currents.\n\n" +
                    "Ground Clearance Requirement (3 Air Blocks):\n" +
                    "Requires AT LEAST 3 air blocks of open vertical clearance directly beneath the bottom edge of the rotor. If ground or structures obstruct this clearance zone, the rotor stalls (0 FE/t).\n\n" +
                    "Altitude-Based Power Scaling:\n" +
                    "The higher above ground the rotor is placed, the faster the wind currents! Generates 10 FE/tick at minimum clearance (3 air blocks), scaling by +2 FE/tick for each additional block of open clearance up to a maximum ceiling of 64 FE/tick.\n\n" +
                    "Weather Wind Surge:\n" +
                    "Automatically gains +20% generation during rain and a +50% storm surge generation bonus during thunderstorms.\n\n" +
                    "Axle Stacking:\n" +
                    "Rotors placed side-by-side along their axle axis form a continuous wind farm drive shaft line, pooling power across the stack.",
                    "Build high towers or mount rotors on cliff edges to maximize air clearance and easily reach the 64 FE/t output cap!",
                    reqs,
                    layers,
                    recipes
            ));
        }

        // =========================================================================
        // 3. SOLAR PANEL ARRAYS (Standard, Gold 2x, Diamond 5x)
        // =========================================================================
        {
            List<GuideRecipe> recipes = List.of(
                    new GuideRecipe("Standard Solar Panel (1 FE/t)",
                            List.of(
                                    () -> mcItem("glass", 3),
                                    () -> mcItem("lapis_lazuli", 2),
                                    () -> mcItem("quartz", 1),
                                    () -> mcItem("iron_ingot", 2),
                                    () -> mcItem("redstone", 1)
                            ),
                            List.of(() -> new ItemStack(PowerGridMod.SOLAR_PANEL_ITEM.get())),
                            "Baseline Photovoltaic Generator (10,000 FE Buffer)"),
                    new GuideRecipe("Gold Solar Panel (2 FE/t - 2x)",
                            List.of(
                                    () -> new ItemStack(PowerGridMod.SOLAR_PANEL_ITEM.get()),
                                    () -> mcItem("gold_ingot", 4)
                            ),
                            List.of(() -> new ItemStack(PowerGridMod.GOLD_SOLAR_PANEL_ITEM.get())),
                            "Surround Standard Solar Panel with 4x Gold Ingots"),
                    new GuideRecipe("Diamond Solar Panel (5 FE/t - 5x)",
                            List.of(
                                    () -> new ItemStack(PowerGridMod.GOLD_SOLAR_PANEL_ITEM.get()),
                                    () -> mcItem("diamond", 4)
                            ),
                            List.of(() -> new ItemStack(PowerGridMod.DIAMOND_SOLAR_PANEL_ITEM.get())),
                            "Surround Gold Solar Panel with 4x Diamonds")
            );

            register(new GuideEntry(
                    "solar_panels",
                    "Solar Panel Arrays",
                    "Photovoltaic Monoliths (1x, 2x Gold, 5x Diamond)",
                    GuideCategory.GENERATION,
                    () -> new ItemStack(PowerGridMod.SOLAR_PANEL_ITEM.get()),
                    "Low-profile 1x1 photovoltaic panels featuring under-deck inverter chassis, heatsink cooling fins, and corner isolation feet.\n\n" +
                    "Three Progressive Tiers:\n" +
                    "• Standard: 1 FE/t daylight generation, 10,000 FE buffer, 100 FE/t export limit.\n" +
                    "• Gold (2x): 2 FE/t daylight generation, 20,000 FE buffer, 200 FE/t export limit. Polished gold bezel.\n" +
                    "• Diamond (5x): 5 FE/t daylight generation, 50,000 FE buffer, 500 FE/t export limit. Platinum bezel.\n\n" +
                    "Connected Multi-Block Array Architecture:\n" +
                    "Placing identical solar panels side-by-side automatically merges their outer bezels, extending continuous 3D silver busbars across cells and forming shared under-deck structural trusses.\n\n" +
                    "Omnidirectional Power Export:\n" +
                    "Central bottom port and all four side faces expose standard Forge Energy, directly feeding adjacent transformers, accumulators, AWG cables, or wire connectors.",
                    "Connected arrays automatically balance energy between panels. Power outputs from all 4 sides and the bottom; the top glass is sealed.",
                    null,
                    null,
                    recipes
            ));
        }

        // =========================================================================
        // 4. THREE-TIER VOLTAGE GRID (Transmission)
        // =========================================================================
        {
            List<GuideRecipe> recipes = List.of(
                    new GuideRecipe("LV Copper Wire Coil",
                            List.of(() -> mcItem("copper_ingot", 4), () -> mcItem("stick", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.LV_WIRE_COIL.get(), 4)),
                            "Low Voltage (400 FE/t, 16 blocks max span)"),
                    new GuideRecipe("MV Electrum Wire Coil",
                            List.of(() -> mcItem("gold_ingot", 2), () -> mcItem("copper_ingot", 2), () -> mcItem("stick", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.MV_WIRE_COIL.get(), 4)),
                            "Medium Voltage (2,048 FE/t, 32 blocks max span)"),
                    new GuideRecipe("HV Steel-Core Cable Coil",
                            List.of(() -> mcItem("iron_ingot", 4), () -> mcItem("stick", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.HV_WIRE_COIL.get(), 4)),
                            "High Voltage (16,384 FE/t, 64 blocks max span)")
            );

            register(new GuideEntry(
                    "voltage_tiers",
                    "Three-Tier Voltage Grid",
                    "Low, Medium, and High Voltage Networks",
                    GuideCategory.TRANSMISSION,
                    () -> new ItemStack(PowerGridMod.LV_WIRE_COIL.get()),
                    "Power Grid divides electrical transmission into three distinct industrial voltage classes:\n\n" +
                    "• Low Voltage (LV - Copper):\n" +
                    "  - Maximum Throughput: 400 FE/tick\n" +
                    "  - Maximum Span: 16 blocks\n" +
                    "  - Line Resistance: 2.0% per block (loss-free under 8 blocks)\n" +
                    "  - Best for local workshops, lighting, and low-draw machines.\n\n" +
                    "• Medium Voltage (MV - Electrum):\n" +
                    "  - Maximum Throughput: 2,048 FE/tick\n" +
                    "  - Maximum Span: 32 blocks\n" +
                    "  - Line Resistance: 1.0% per block (loss-free under 16 blocks)\n" +
                    "  - Ideal for factory sub-mains and processing machinery.\n\n" +
                    "• High Voltage (HV - Steel-Core):\n" +
                    "  - Maximum Throughput: 16,384 FE/tick\n" +
                    "  - Maximum Span: 64 blocks\n" +
                    "  - Line Resistance: 0.5% per block (loss-free under 24 blocks)\n" +
                    "  - Built for cross-country bulk power transmission.",
                    "Always step up to High Voltage for long-distance runs! HV lines experience 4x less resistance loss than LV lines.",
                    null,
                    null,
                    recipes
            ));
        }

        // =========================================================================
        // 5. POLES, TOWERS & CROSSARMS (Transmission)
        // =========================================================================
        {
            List<GuideRecipe> recipes = List.of(
                    new GuideRecipe("Treated Wood Power Pole",
                            List.of(() -> new ItemStack(PowerGridMod.TREATED_WOOD.get(), 3)),
                            List.of(() -> new ItemStack(PowerGridMod.TREATED_WOOD_POLE_ITEM.get(), 6)),
                            "Vertical row of 3x Treated Wood Planks"),
                    new GuideRecipe("Power Pole Crossarm",
                            List.of(() -> new ItemStack(PowerGridMod.TREATED_WOOD.get(), 3), () -> mcItem("stick", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.POLE_CROSSARM_ITEM.get(), 2)),
                            "3x Treated Wood Planks + 1x Stick"),
                    new GuideRecipe("Steel Truss High-Voltage Tower",
                            List.of(() -> mcItem("iron_bars", 4), () -> mcItem("iron_ingot", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.STEEL_TRUSS_POLE_ITEM.get(), 6)),
                            "Iron Ingot surrounded by 4x Iron Bars")
            );

            register(new GuideEntry(
                    "poles_and_towers",
                    "Poles, Towers & Crossarms",
                    "Overhead Catenary Infrastructure",
                    GuideCategory.TRANSMISSION,
                    () -> new ItemStack(PowerGridMod.TREATED_WOOD_POLE_ITEM.get()),
                    "Heavy structural supports engineered to suspend physical catenary transmission wires:\n\n" +
                    "• Treated Wood Utility Poles:\n" +
                    "  Authentic 8-sided cylindrical wooden telephone poles with steel bands and climbing rungs. Right-clicking with a pole item auto-stacks upwards!\n\n" +
                    "• Modular Pole Crossarms:\n" +
                    "  Horizontal timber crossarms that automatically clamp to adjacent poles with forged iron collars and connect seamlessly across neighboring crossarms. Right-clicking auto-extends horizontally.\n\n" +
                    "• Top-of-Pole Riser Mounting:\n" +
                    "  Placing a crossarm directly on top of a pole creates a heavy steel mounting collar with a vertical structural riser column down to the pole cap.\n\n" +
                    "• Steel Truss Lattice Towers:\n" +
                    "  Massive, blast-resistant high-voltage transmission towers for overland cable runs. Supports right-click auto-stacking.",
                    "Climbing rungs on wooden poles let engineers climb up to make connections without needing ladders!",
                    null,
                    null,
                    recipes
            ));
        }

        // =========================================================================
        // 6. CONNECTORS & RELAYS (Transmission)
        // =========================================================================
        {
            List<GuideRecipe> recipes = List.of(
                    new GuideRecipe("LV Wire Connector",
                            List.of(() -> mcItem("copper_ingot", 2), () -> new ItemStack(PowerGridMod.CERAMIC_INSULATOR.get(), 1)),
                            List.of(() -> new ItemStack(PowerGridMod.LV_WIRE_CONNECTOR_ITEM.get(), 4)),
                            "2x Copper Ingot + 1x Ceramic Insulator"),
                    new GuideRecipe("LV Wire Relay",
                            List.of(() -> new ItemStack(PowerGridMod.CERAMIC_INSULATOR.get(), 2)),
                            List.of(() -> new ItemStack(PowerGridMod.LV_WIRE_RELAY_ITEM.get(), 4)),
                            "2x Ceramic Insulator (Pass-through corner routing)")
            );

            register(new GuideEntry(
                    "connectors_and_relays",
                    "Connectors & Relays",
                    "Terminals, Feeders & Pass-Through Insulators",
                    GuideCategory.TRANSMISSION,
                    () -> new ItemStack(PowerGridMod.LV_WIRE_CONNECTOR_ITEM.get()),
                    "Terminals and insulators for attaching catenary wire coils:\n\n" +
                    "• Wire Connectors (Tap Nodes):\n" +
                    "  Mount directly on machines, accumulators, or transformers. Automatically pushes or pulls Forge Energy through the attached block's buffer.\n" +
                    "  - Modes: Right-click with Grid Engineer's Hammer to cycle AUTO, INPUT (draw power into line), OUTPUT (push line power into block).\n\n" +
                    "• Wire Relays (Pass-Through Insulators):\n" +
                    "  High-density pass-through insulators designed to turn corners and route branches across poles without interacting with adjacent blocks. Relays support up to 8 wire connections.\n\n" +
                    "• Specialized Mount Geometries:\n" +
                    "  - Crossarm-End: Perfectly centers insulators on the beam end face.\n" +
                    "  - Pole-Mount: Extends heavy structural steel standoff channel brackets to eliminate floating air gaps.",
                    "Use Relays on crossarms to turn corners with zero energy loss. Connectors should only be used where you want to inject or extract power.",
                    null,
                    null,
                    recipes
            ));
        }

        // =========================================================================
        // 7. SUBSTATION TRANSFORMER (Substation)
        // =========================================================================
        {
            List<GuideRecipe> recipes = List.of(
                    new GuideRecipe("Substation Transformer",
                            List.of(
                                    () -> mcItem("iron_ingot", 4),
                                    () -> mcItem("copper_ingot", 4),
                                    () -> mcItem("redstone", 1)
                            ),
                            List.of(() -> new ItemStack(PowerGridMod.TRANSFORMER_ITEM.get())),
                            "ICI / CRC / ICI in Crafting Table")
            );

            register(new GuideEntry(
                    "transformer",
                    "Substation Transformer",
                    "98% Efficient Voltage Step-Up / Step-Down",
                    GuideCategory.SUBSTATION,
                    () -> new ItemStack(PowerGridMod.TRANSFORMER_ITEM.get()),
                    "High-efficiency industrial substation transformer connecting different voltage tiers:\n\n" +
                    "• Tier Conversions:\n" +
                    "  - Low Voltage (LV) ⇄ Medium Voltage (MV)\n" +
                    "  - Medium Voltage (MV) ⇄ High Voltage (HV)\n\n" +
                    "• 98% Industrial Efficiency:\n" +
                    "  Operates at 98% conversion efficiency (2% transformer core loss).\n\n" +
                    "• Throughput Capacity:\n" +
                    "  Transfers up to 16,384 FE/tick matching High Voltage line speed.\n\n" +
                    "• Intelligent Isolation:\n" +
                    "  Prevents back-feeding and storage-to-storage oscillation loops while routing energy cleanly between networks.",
                    "Step power up at your central generator station (LV → HV), transmit cross-country on Steel Towers, and step down (HV → LV) at your remote base!",
                    null,
                    null,
                    recipes
            ));
        }

        // =========================================================================
        // 8. HEAVY ACCUMULATOR BANK (Substation)
        // =========================================================================
        {
            List<GuideRecipe> recipes = List.of(
                    new GuideRecipe("Heavy Accumulator Bank",
                            List.of(
                                    () -> mcItem("iron_ingot", 6),
                                    () -> mcItem("redstone_block", 2),
                                    () -> mcItem("copper_block", 1)
                            ),
                            List.of(() -> new ItemStack(PowerGridMod.HEAVY_ACCUMULATOR_ITEM.get())),
                            "III / RBR / III in Crafting Table")
            );

            register(new GuideEntry(
                    "heavy_accumulator",
                    "Heavy Accumulator Bank",
                    "4,000,000 FE Industrial Energy Storage",
                    GuideCategory.SUBSTATION,
                    () -> new ItemStack(PowerGridMod.HEAVY_ACCUMULATOR_ITEM.get()),
                    "Industrial multi-cell capacitor storage bank for grid buffering:\n\n" +
                    "• Massive Capacity:\n" +
                    "  Stores up to 4,000,000 FE per block.\n\n" +
                    "• Rapid Charge / Discharge:\n" +
                    "  Supports up to 8,192 FE/tick transfer rate per face.\n\n" +
                    "• Intra-Bank Protection:\n" +
                    "  Adjacent accumulator blocks automatically cluster into unified storage banks without parasitic sloshing loops.",
                    "Inspect charge levels and live throughput anytime using the Grid Voltmeter!",
                    null,
                    null,
                    recipes
            ));
        }

        // =========================================================================
        // 9. HIGH VOLTAGE BREAKER SWITCH (Substation)
        // =========================================================================
        {
            List<GuideRecipe> recipes = List.of(
                    new GuideRecipe("High Voltage Breaker Switch",
                            List.of(
                                    () -> mcItem("iron_ingot", 7),
                                    () -> mcItem("lever", 1),
                                    () -> mcItem("redstone", 1)
                            ),
                            List.of(() -> new ItemStack(PowerGridMod.BREAKER_SWITCH_ITEM.get())),
                            "III / ILI / IRI in Crafting Table")
            );

            register(new GuideEntry(
                    "breaker_switch",
                    "HV Breaker Switch",
                    "Wall-Mounted Industrial Disconnect Cabinet",
                    GuideCategory.SUBSTATION,
                    () -> new ItemStack(PowerGridMod.BREAKER_SWITCH_ITEM.get()),
                    "Heavy-duty industrial disconnect safety switch modeled after commercial Siemens/Square-D NEMA enclosures:\n\n" +
                    "• Physical Throw Handle:\n" +
                    "  - UP Position (ON): Circuit is closed, conducting full line current between terminals.\n" +
                    "  - DOWN Position (OFF): Physical air-gap disconnect severs the circuit with zero leakage.\n\n" +
                    "• Manual & Redstone Control:\n" +
                    "  Right-click handle to toggle manually, or drive with Redstone for automated remote trip isolation.\n\n" +
                    "• Universal Line Support:\n" +
                    "  Supports in-line connections for LV, MV, and HV wire lines.",
                    "Place breaker switches between your power generation stations and transmission lines so you can safely shut down grids for maintenance.",
                    null,
                    null,
                    recipes
            ));
        }

        // =========================================================================
        // 10. INDOOR AWG POWER CABLES (Distribution)
        // =========================================================================
        {
            List<GuideRecipe> recipes = List.of(
                    new GuideRecipe("18 AWG Cable (Copper - 128 FE/t)",
                            List.of(() -> mcItem("dried_kelp", 6), () -> mcItem("copper_ingot", 3)),
                            List.of(() -> new ItemStack(PowerGridMod.CABLE_18_GAUGE_ITEM.get(), 8)),
                            "KKK / CCC / KKK with Dried Kelp or Rubber"),
                    new GuideRecipe("16 AWG Cable (Iron - 512 FE/t)",
                            List.of(() -> mcItem("dried_kelp", 6), () -> mcItem("iron_ingot", 3)),
                            List.of(() -> new ItemStack(PowerGridMod.CABLE_16_GAUGE_ITEM.get(), 8)),
                            "KKK / III / KKK with Dried Kelp or Rubber"),
                    new GuideRecipe("14 AWG Cable (Steel - 2,048 FE/t)",
                            List.of(() -> mcItem("dried_kelp", 6), () -> new ItemStack(PowerGridMod.STEEL_STRAND.get(), 3)),
                            List.of(() -> new ItemStack(PowerGridMod.CABLE_14_GAUGE_ITEM.get(), 8)),
                            "KKK / SSS / KKK with Steel Strands or Steel Ingots"),
                    new GuideRecipe("12 AWG Cable (Gold - 8,192 FE/t)",
                            List.of(() -> mcItem("dried_kelp", 6), () -> mcItem("gold_ingot", 3)),
                            List.of(() -> new ItemStack(PowerGridMod.CABLE_12_GAUGE_ITEM.get(), 8)),
                            "KKK / GGG / KKK with Dried Kelp or Rubber")
            );

            register(new GuideEntry(
                    "awg_cables",
                    "Indoor AWG Power Cables",
                    "Modular 6-Way Machine Wiring (18, 16, 14, 12 AWG)",
                    GuideCategory.DISTRIBUTION,
                    () -> new ItemStack(PowerGridMod.CABLE_14_GAUGE_ITEM.get()),
                    "Modular 6-way indoor power cables for routing electricity directly between machinery, accumulators, and workshops without overhead poles.\n\n" +
                    "Four Gauge Tiers:\n" +
                    "• 18 AWG (Copper): 128 FE/t transfer limit, 128 FE buffer.\n" +
                    "• 16 AWG (Iron): 512 FE/t transfer limit, 512 FE buffer.\n" +
                    "• 14 AWG (Steel): 2,048 FE/t transfer limit, 2,048 FE buffer.\n" +
                    "• 12 AWG (Gold): 8,192 FE/t transfer limit, 8,192 FE buffer.\n\n" +
                    "Ultra-Low Conductance Buffer:\n" +
                    "1-tick buffer transfers power instantaneously without acting as a battery.\n\n" +
                    "Side Isolation:\n" +
                    "Right-click any cable arm with the Engineer's Hammer to isolate or disconnect that face.",
                    "Use Insulated Wire Cutters to instantly snip and recover power cables in survival mode!",
                    null,
                    null,
                    recipes
            ));
        }

        // =========================================================================
        // 11. REFINERY PIPE FACADES (Distribution)
        // =========================================================================
        {
            register(new GuideEntry(
                    "refinery_facades",
                    "Refinery Pipe Facades",
                    "Conceal Cables Inside Walls & Floors",
                    GuideCategory.DISTRIBUTION,
                    () -> new ItemStack(PowerGridMod.CABLE_18_GAUGE_ITEM.get()),
                    "Power Grid AWG cables are 100% compatible with Pipe Facades from the Industrial Refinery mod across all 4 cable gauges (12, 14, 16, 18 AWG):\n\n" +
                    "• Solid Facade Panels (`refinery:pipe_facade`):\n" +
                    "  Right-click any cable face with a solid panel to completely hide the cable flush inside walls, ceilings, and floors with dynamic block texture matching and full collision.\n\n" +
                    "• Hollow Pass-Through Frames (`refinery:pipe_facade_hole`):\n" +
                    "  Neatly frames cables passing through walls with a 6.4x6.4 pixel opening.\n\n" +
                    "• Quick Removal:\n" +
                    "  Sneak-right-click with an empty hand, Engineer's Hammer, or Refinery Wrench to pop facades off.",
                    "Combine with painted facades to create completely invisible wiring conduits in modern factory builds!",
                    null,
                    null,
                    List.of()
            ));
        }

        // =========================================================================
        // 12. ILLUMINATION SUITE (Lighting)
        // =========================================================================
        {
            List<GuideRecipe> recipes = List.of(
                    new GuideRecipe("Tripod Work Light",
                            List.of(() -> mcItem("redstone_lamp", 2), () -> mcItem("glowstone", 1), () -> mcItem("yellow_dye", 1), () -> mcItem("iron_ingot", 2)),
                            List.of(() -> new ItemStack(PowerGridMod.TRIPOD_WORK_LIGHT_ITEM.get())),
                            "2-Block Job-Site Dual Halogen Floodlight (Light Level 15)"),
                    new GuideRecipe("Ceiling Shop Light",
                            List.of(() -> mcItem("iron_ingot", 3), () -> mcItem("glowstone", 3), () -> mcItem("redstone", 2)),
                            List.of(() -> new ItemStack(PowerGridMod.CEILING_SHOP_LIGHT_ITEM.get(), 2)),
                            "2-Block Commercial Dual-Tube Fluorescent Fixture (Light Level 15)"),
                    new GuideRecipe("Wall Floodlight",
                            List.of(() -> mcItem("iron_ingot", 1), () -> mcItem("redstone_lamp", 2), () -> mcItem("quartz", 1), () -> mcItem("redstone", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.WALL_FLOODLIGHT_ITEM.get())),
                            "Dual Angled Security Floodlight with PIR Sensor (Light Level 15)"),
                    new GuideRecipe("Carriage Lantern",
                            List.of(() -> mcItem("iron_ingot", 2), () -> mcItem("glass_pane", 2), () -> mcItem("torch", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.CARRIAGE_LANTERN_ITEM.get())),
                            "Ornate Cast-Iron Coach Lantern (Warm Light Level 15)"),
                    new GuideRecipe("HPS Street Light",
                            List.of(() -> mcItem("glowstone", 1), () -> mcItem("iron_ingot", 2), () -> mcItem("smooth_stone", 2), () -> mcItem("redstone", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.STREET_LIGHT_ITEM.get())),
                            "4-Block Cobrahead Street Light with Dusk-to-Dawn Photocell")
            );

            register(new GuideEntry(
                    "lighting_suite",
                    "Illumination Suite",
                    "Job-Site, Shop, Wall & Municipal Street Lights",
                    GuideCategory.LIGHTING,
                    () -> new ItemStack(PowerGridMod.TRIPOD_WORK_LIGHT_ITEM.get()),
                    "Architectural and industrial lighting fixtures for factories, roads, and yards:\n\n" +
                    "• Tripod Work Light:\n" +
                    "  2-block-tall industrial job-site dual halogen floodlight mounted on a safety-yellow telescoping tripod. Right-click to toggle on/off.\n\n" +
                    "• Ceiling Shop Light:\n" +
                    "  2-block commercial dual-tube fluorescent fixture mounted flush to ceilings. Right-click to toggle tubes.\n\n" +
                    "• Wall Floodlight:\n" +
                    "  Outdoor security floodlight with dark bronze base, dual angled floodlight heads, and central PIR motion-sensor styling.\n\n" +
                    "• Carriage Lantern:\n" +
                    "  Traditional cast-iron exterior wall lantern featuring an ornate bracket, pagoda hood, and glowing incandescent bulb.\n\n" +
                    "• HPS Street Light:\n" +
                    "  4-block municipal street light with concrete footing, davit mast arm, and cobrahead luminaire. Base connects directly to power cables or wire connectors.",
                    "Right-click any work light or lantern to toggle illumination! Street lights can toggle between Dusk-to-Dawn photocell sensor and 24/7 Always Active mode using the Engineer's Hammer.",
                    null,
                    null,
                    recipes
            ));
        }

        // =========================================================================
        // 13. GRID ENGINEER'S TOOLKIT (Tools)
        // =========================================================================
        {
            List<GuideRecipe> recipes = List.of(
                    new GuideRecipe("Grid Engineer's Field Manual",
                            List.of(() -> mcItem("book", 1), () -> mcItem("copper_ingot", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.GUIDE_BOOK.get())),
                            "Book + Copper Ingot (or Copper Wire Strand)"),
                    new GuideRecipe("Grid Engineer's Hammer",
                            List.of(() -> mcItem("iron_ingot", 1), () -> mcItem("stick", 2)),
                            List.of(() -> new ItemStack(PowerGridMod.ENGINEER_HAMMER.get())),
                            "Essential tool for rotating blocks, cycling modes, and isolating cables"),
                    new GuideRecipe("Grid Voltmeter",
                            List.of(() -> mcItem("copper_ingot", 1), () -> mcItem("redstone", 2), () -> mcItem("glass_pane", 1), () -> mcItem("iron_ingot", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.VOLTMETER.get())),
                            "Live diagnostic multimeter reporting voltage, FE/t, and topology"),
                    new GuideRecipe("Insulated Wire Cutters",
                            List.of(() -> mcItem("iron_ingot", 2), () -> mcItem("stick", 3)),
                            List.of(() -> new ItemStack(PowerGridMod.WIRE_CUTTERS.get())),
                            "Safely sever wire connections and recover wire coils; instant cable clipping")
            );

            register(new GuideEntry(
                    "engineering_tools",
                    "Grid Engineer's Toolkit",
                    "Manual, Hammer, Voltmeter & Insulated Wire Cutters",
                    GuideCategory.TOOLS,
                    () -> new ItemStack(PowerGridMod.ENGINEER_HAMMER.get()),
                    "Specialized engineering tools for constructing, configuring, and maintaining electrical networks:\n\n" +
                    "• Grid Engineer's Field Manual:\n" +
                    "  Comprehensive guide with schematics, bill of materials, layer-by-layer visualizers, and recipe references.\n\n" +
                    "• Grid Engineer's Hammer:\n" +
                    "  - Right-click directional blocks (water wheels, windmill rotors, street lights) to rotate 90 degrees.\n" +
                    "  - Right-click Wire Connectors to cycle AUTO, INPUT, and OUTPUT modes.\n" +
                    "  - Right-click AWG cable faces to isolate or disconnect connections.\n\n" +
                    "• Grid Voltmeter:\n" +
                    "  Live electrical multimeter. Right-click any block, pole, connector, accumulator, solar panel, water wheel, windmill rotor, or cable to inspect live voltage tier, stored FE, throughput, clearance, and network topology.\n\n" +
                    "• Insulated Wire Cutters:\n" +
                    "  Safely and cleanly disconnects all attached wire lines from any connector or relay, refunding wire coils into your inventory. Instantly removes AWG power cables.",
                    "Always keep a Voltmeter and Hammer on your hotbar when designing high-voltage grids!",
                    null,
                    null,
                    recipes
            ));
        }

        // =========================================================================
        // 14. MATERIALS & CRAFTING (Tools)
        // =========================================================================
        {
            List<GuideRecipe> recipes = List.of(
                    new GuideRecipe("Treated Wood Planks",
                            List.of(() -> mcItem("oak_planks", 8), () -> mcItem("coal", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.TREATED_WOOD.get(), 8)),
                            "8x Planks around 1x Coal or Charcoal"),
                    new GuideRecipe("Copper Wire Strand",
                            List.of(() -> mcItem("copper_ingot", 3)),
                            List.of(() -> new ItemStack(PowerGridMod.COPPER_WIRE.get(), 6)),
                            "3x Copper Ingot in a row (or 1x shapeless yields 2x)"),
                    new GuideRecipe("Electrum Wire Strand",
                            List.of(() -> mcItem("gold_ingot", 1), () -> mcItem("copper_ingot", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.ELECTRUM_WIRE.get(), 4)),
                            "Shapeless 1x Gold Ingot + 1x Copper Ingot"),
                    new GuideRecipe("Reinforced Steel Strand",
                            List.of(() -> mcItem("iron_ingot", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.STEEL_STRAND.get(), 1)),
                            "Smelt or blast Iron Ingot into Steel Strand"),
                    new GuideRecipe("Ceramic High-Voltage Insulator",
                            List.of(() -> mcItem("clay_ball", 1), () -> mcItem("terracotta", 1), () -> mcItem("iron_ingot", 1)),
                            List.of(() -> new ItemStack(PowerGridMod.CERAMIC_INSULATOR.get(), 4)),
                            "Clay Ball + Terracotta + Iron Ingot")
            );

            register(new GuideEntry(
                    "materials_crafting",
                    "Materials & Crafting",
                    "Treated Wood, Conductor Strands & Insulators",
                    GuideCategory.TOOLS,
                    () -> new ItemStack(PowerGridMod.TREATED_WOOD.get()),
                    "Essential materials for crafting electrical infrastructure:\n\n" +
                    "• Treated Wood Planks:\n" +
                    "  Creosote-treated durable lumber crafted by infusing wooden planks with coal or charcoal. Water-resistant and immune to weathering.\n\n" +
                    "• Copper Wire Strand:\n" +
                    "  Drawn copper conductor wire for Low Voltage (LV) distribution and coils.\n\n" +
                    "• Electrum Wire Strand:\n" +
                    "  High-conductivity gold-copper alloy wire for Medium Voltage (MV) transmission.\n\n" +
                    "• Reinforced Steel Strand:\n" +
                    "  High-tensile steel core for High Voltage (HV) long-distance catenary cables.\n\n" +
                    "• Ceramic High-Voltage Insulator:\n" +
                    "  Weather-resistant glazed porcelain bushing preventing electrical arc grounding.",
                    "Treated wood planks can be crafted 8 at a time on a crafting table, or 4 at a time in your 2x2 player inventory!",
                    null,
                    null,
                    recipes
            ));
        }
    }

    private static void register(GuideEntry entry) {
        ENTRIES.add(entry);
        BY_ID.put(entry.getId(), entry);
    }

    public static List<GuideEntry> getAllEntries() {
        init();
        return Collections.unmodifiableList(ENTRIES);
    }

    public static List<GuideEntry> getEntriesByCategory(GuideCategory category) {
        init();
        if (category == GuideCategory.ALL) {
            return Collections.unmodifiableList(ENTRIES);
        }
        return ENTRIES.stream().filter(e -> e.getCategory() == category).toList();
    }

    public static @Nullable GuideEntry getEntry(String id) {
        init();
        return BY_ID.get(id);
    }
}

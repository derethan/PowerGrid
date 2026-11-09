# Changelog — Power Grid

All notable changes to the **Power Grid** mod are documented in this file.

---

## [1.7.6] - 2026-11-08

### Fixed
- **AWG Cable Energy Router Generator Demand Probing (CableNetworkRouter)**:
  - Fixed an issue where distributeFromCable simulated extraction from attached power sources (such as steam turbines, diesel generators, or batteries) on every tick before verifying whether any connected sinks had available storage capacity.
  - Sinks are now probed first; if all connected energy storage banks (such as the Heavy Accumulator Bank) are fully charged and the grid has 0 demand, the router immediately skips without querying or simulating extraction from the generator.
  - Prevents continuous simulation calls that could trigger ghost-drain bugs in attached power generators when connected to fully-charged energy storage.
- **Solar Panel Energy Storage Transaction Handling (SolarPanelBlockEntity)**:
  - Replaced manual state assignments in insert and xtract with NeoForge's standard onEnergyChanged(int previousAmount) handler on SimpleEnergyHandler.
  - Ensures simulated transactions automatically roll back without leaving phantom energy modifications or corrupting solar panel energy storage.

---

## [1.7.5] - 2026-11-08

### Fixed
- **Item Display Names & Translation Keys in UI Tooltips**:
  - Resolved an issue where block items (such as the LV Wire Relay, connectors, towers, transformers, and switches) displayed unlocalized variable-like translation keys (e.g., `item.powergrid.lv_wire_relay`) in creative tabs, inventories, and modded viewers.
  - Added full dual registration in `en_us.json` ensuring both `item.powergrid.<id>` and `block.powergrid.<id>` translations exist for every single item and block in the mod.
  - Configured all custom BlockItem implementations (`PowerGridBlockItem`, `StreetLightBlockItem`, `PowerCableBlockItem`, `WoodenWaterWheelBlockItem`, and `WindmillRotorBlockItem`) with `properties.useBlockDescriptionPrefix()` to match standard vanilla block item naming semantics.

---
## [1.7.3] - 2026-11-08

### Added
- **Refinery Mod Petroleum Tar AWG Cable Recipes**:
  - Registered `c:tar` common item tag in data (`data/c/tags/item/tar.json` and `data/c/tags/items/tar.json`) with support for `refinery:tar` (Petroleum Tar from Industrial Refinery) and Logistics tar.
  - Added alternative crafting recipes for all 4 indoor AWG power cables using Petroleum Tar as the insulating jacket:
    - **18 AWG Copper Power Cable**: Craftable with 6x Petroleum Tar (`#c:tar`) and 3x Copper Ingot or Copper Wire Strand (`cable_18_gauge_from_tar` and `cable_18_gauge_from_wire_tar`).
    - **16 AWG Iron Power Cable**: Craftable with 6x Petroleum Tar (`#c:tar`) and 3x Iron Ingot (`cable_16_gauge_from_tar`).
    - **14 AWG Steel Power Cable**: Craftable with 6x Petroleum Tar (`#c:tar`) and 3x Reinforced Steel Strand or Steel Ingot (`cable_14_gauge_from_tar`).
    - **12 AWG Gold Power Cable**: Craftable with 6x Petroleum Tar (`#c:tar`) and 3x Gold Ingot or Electrum Wire Strand (`cable_12_gauge_from_tar` and `cable_12_gauge_from_wire_tar`).
  - Added wire strand crafting recipes with rubber insulation for 18 AWG and 12 AWG cables (`cable_18_gauge_from_wire_rubber` and `cable_12_gauge_from_wire_rubber`).
  - Implemented safe conditional recipe gating (`neoforge:conditions` with `neoforge:tag_empty` check on `c:tar`) so recipes automatically activate when Refinery is installed without throwing missing tag errors in vanilla environments.
  - Updated the in-game Grid Engineer's Field Manual cable recipe codex cards to describe crafting cables with Dried Kelp, Rubber, or Petroleum Tar.

---

## [1.7.1] - 2026-11-07

### Added
- **Comprehensive Item & Block Tooltips**:
  - Added rich, multi-line, color-coded informational tooltips to all blocks and items across Power Grid:
    - **Poles & Crossarms**: Added functional tooltips for Treated Wood Poles, Steel Truss Towers, and Pole Crossarms detailing structural characteristics and right-click auto-extension.
    - **Connectors & Relays**: Added voltage tier, throughput rating (400, 2,048, 16,384 FE/t), connection limits (4 for connectors, 8 for relays), and Engineer's Hammer mode-cycling tips.
    - **Transformers & Switchgear**: Added substation converter specifications (64,000 FE buffer, 16,384 FE/t transfer) and High Voltage Breaker Switch circuit controls.
    - **Heavy Accumulator Bank**: Added energy storage capacities (4,000,000 FE) and auto-discharge behavior.
    - **Solar Panels**: Added daytime generation rates (1 FE/t, 2 FE/t for Gold, 5 FE/t for Diamond), buffer capacities, and array energy-balancing notes.
    - **Work-Site & Architectural Illumination**: Added light level output (Level 15), right-click switch behavior, and redstone automation support for Tripod Work Lights, Ceiling Shop Lights, Wall Floodlights, and Carriage Lanterns.
    - **Indoor Power Cables**: Added tooltip hints for configuring cable face connections using the Engineer's Hammer.
    - **Insulated Wire Cutters**: Added secondary tooltip describing instant snipping and retrieval of indoor AWG power cables.
    - **Engineering Insignia**: Added localized tooltip description for Grid Icon.
- **PowerGridBlockItem Framework**:
  - Created reusable `PowerGridBlockItem` component supporting static or supplier-based localized tooltips with varargs convenience constructors.

### Changed
- Standardized item hover text across the mod to match the dark industrial aesthetic of the Grid Engineer's Field Manual.

---

## [1.7.0] - 2026-11-07

### Added
- **Grid Engineer's Field Manual (`powergrid:guide_book`)**:
  - Interactive industrial engineering guidebook matching the visual aesthetic and architecture of the *Refinery* and *Track and Trains* manuals.
  - **Dark Slate & Galvanized Steel Industrial Screen**:
    - Modeled with riveted steel bezel borders (`0xFF374151`), heavy brass corner brackets (`0xFFF59E0B`, `0xFFFCD34D`), and dark slate binder styling (`0xFF0F1115`, `0xFF181B20`).
    - Authentic page-turning audio and tactile button feedback (`SoundEvents.BOOK_PAGE_TURN`, `SoundEvents.UI_BUTTON_CLICK`).
  - **Left Category Sidebar & Topics Table of Contents**:
    - Category browsing across 7 dedicated sections: `All Contents`, `Power Generation`, `Overhead Grid`, `Substation & Storage`, `Indoor Wiring`, `Illumination Suite`, and `Tools & Materials`.
    - Topic cards with live item icons, titles, subtitles, topic count badge, and smooth scrollbar.
  - **Multiblock Layout & Blueprint Visualizer**:
    - Full 2D top-down and front-facing layer visualizers for the `Wooden Water Wheel` and `Windmill Rotor` multiblocks.
    - Interactive layer selection pills (`L0`), symbol grid cells, relative coordinate inspection tooltips, and block legend.
    - Bill of Materials tab with item icons, item quantities, and total block count badges.
  - **Engineering Overview & Recipes**:
    - Comprehensive technical specifications, pro-tips, voltage formulas, clearance rules, and loss-free span distance references.
    - Interactive recipe cards displaying input items $\rightarrow$ arrow $\rightarrow$ output items with full hover tooltips and crafting notes.
  - **Survival Crafting & HD Pack**:
    - Shapeless crafting: Book (`minecraft:book`) + Copper Ingot or Copper Wire Strand (`powergrid:copper_wire`) $\rightarrow$ Field Manual.
    - Custom 32x32 pixel art texture and 1024x1024 Ultra-HD pack texture with dark leather cover, brass brackets, and electric lightning insignia.

---

## [1.6.0] - 2026-11-07

### Added
- **Pole-Mounted High-Pressure Sodium (HPS) Street Lights (`powergrid:pole_street_light`)**:
  - Overhead utility pole attachment fixture modeled after classic North American municipal cobrahead street lights mounted to wooden or steel utility poles.
  - **Exclusively Powered via Overhead Utility Wires**:
    - Implements `IWireNode` with a dedicated ceramic insulator bushing wire terminal mounted atop the structural steel pole clamp bracket.
    - Connects directly to Low Voltage (LV), Medium Voltage (MV), and High Voltage (HV) wire coils, drawing power directly from overhead transmission lines.
    - Does not connect to indoor/underground power cables, reserving its electrical input strictly for overhead pole circuits.
    - Supports up to 2 concurrent wire connections, enabling seamless in-line drops and pole-to-pole daisy-chaining.
  - **Dual-Tier Illumination & Radius**:
    - **Default Standby Mode (Unpowered)**: Emits a warm amber standby illumination (Light Level 7, ~7 block radius) immediately upon placement without needing power.
    - **Active Energized Mode (Powered)**: Emits brilliant high brightness (Light Level 15, full 15-block radius) when energized with 5 FE/t supplied from the overhead pole wires.
  - **Dusk-to-Dawn Photocell Sensor**:
    - Features an authentic NEMA twist-lock photocell sensor on the cobrahead housing.
    - Sneak-Right-Click with the Grid Engineer's Hammer toggles between **Automatic (Dusk-to-Dawn)** (activates only during dark hours or storms) and **Always Active (24/7)** continuous illumination.
    - Normal Right-Click with the Grid Engineer's Hammer rotates the mounting orientation.
  - **Voltmeter Diagnostics**:
    - Inspecting the fixture with the Grid Voltmeter reports energized status, stored energy (`X / 1,000 FE`), active power draw (5 FE/t), photocell mode, and active overhead wire connections.
  - **Survival Crafting Recipe & Ultra-HD 1024x Pack**:
    - Craftable with Glowstone, Iron Ingots, and a Ceramic Insulator.
    - Full procedural 32x32 textures and Ultra-HD 1024x resource pack support with authentic amber sodium-vapor glow.

---

## [1.5.1] - 2026-11-07

### Added
- **Treated Wood Planks Crafting Recipes (`powergrid:treated_wood`)**:
  - Shaped crafting: 8x Planks (`#minecraft:planks`) surrounding 1x Coal or Charcoal (`#minecraft:coals`) yields 8x Treated Wood Planks.
  - Shapeless crafting: 4x Planks + 1x Coal/Charcoal yields 4x Treated Wood Planks for crafting on the go in 2x2 player inventory.
- **Conductor Wire Strand Crafting Recipes**:
  - **Copper Wire Strand (`powergrid:copper_wire`)**: Horizontal row of 3x Copper Ingot yields 6x Copper Wire Strands (or 1x Copper Ingot shapeless yields 2x strands).
  - **Electrum Wire Strand (`powergrid:electrum_wire`)**: Shapeless crafting with 1x Gold Ingot + 1x Copper Ingot yields 4x Electrum Wire Strands (or `GCG` row yields 6x strands).
  - **Reinforced Steel Strand (`powergrid:steel_strand`)**: Added regular furnace smelting (Iron Ingot $\rightarrow$ Steel Strand) and shaped carburizing (3x Iron Ingot + 1x Coal/Charcoal $\rightarrow$ 4x Steel Strands) in addition to blast furnace processing.
- **Alternative Wire Coil & Cable Recipes**:
  - Added shaped crafting recipes for LV, MV, and HV Wire Coils directly using their corresponding wire strands (`copper_wire`, `electrum_wire`, `steel_strand`).
  - Added 18 AWG and 12 AWG power cable recipes using wire strands in addition to raw ingots.
- **Recipe Integrity Test Suite (`RecipeIntegrityTest`)**:
  - Automated test verifying all 37 survival items and blocks registered in Power Grid possess valid, syntactically correct crafting recipes.

### Fixed
- **Water Wheel & Windmill Rotor Missing Recipe Bug**:
  - Resolved an issue where `Wooden Water Wheel` and `Windmill Rotor` could not be crafted in survival mode due to `powergrid:treated_wood` lacking a recipe.
- **Treated Wood Tag & Fallback Compatibility**:
  - Created `#powergrid:treated_wood`, `#c:treated_wood`, and `#c:planks/treated` item tags encompassing `powergrid:treated_wood` with `minecraft:spruce_planks` as a seamless fallback.
  - Updated `wooden_water_wheel`, `windmill_rotor`, `treated_wood_pole`, and `pole_crossarm` recipes to accept `#powergrid:treated_wood`.
- **14 AWG Cable Vanilla Crafting Fix**:
  - Fixed an issue where 14 AWG Cable recipes exclusively required `#c:ingots/steel` which was unattainable in pure vanilla Minecraft without a separate steel mod installed.
  - Created `#powergrid:steel_conductors` allowing 14 AWG cables to craft using `powergrid:steel_strand` or any modded steel ingot.

---

## [1.5.0] - 2026-11-07

### Added
- **Kinetic Windmill Rotor Generator (`powergrid:windmill_rotor`)**:
  - **Large 3x3x1 Footprint**: Authentic aerodynamic windmill rotor multiblock standing 3 blocks high, 3 blocks wide/long, and 1 block deep along its central axle shaft.
  - **Ground Clearance & Altitude Scaling Generation**:
    - Requires **at least 3 air blocks of open vertical clearance** directly beneath the rotor bottom to operate; obstructed ground stops the rotor and produces 0 FE/t.
    - Power generation scales smoothly with height above the minimum clearance threshold: generates a base 10 FE/t at 3 air blocks, +2 FE/t for each additional block of open clearance up to a configurable maximum of 64 FE/t.
    - Weather Wind Boost: Automatically gains a +20% generation bonus during rain and a +50% storm surge generation bonus during thunderstorms.
  - **Central Axle Power Hub & Axle Stacking**:
    - Central axle hub outputs accumulated Forge Energy directly into adjacent transformers, accumulators, AWG cables, or wire connectors via standard NeoForge `Capabilities.Energy.BLOCK`.
    - Rotors placed adjacent to each other along their axle axis automatically stack into a unified drive shaft line, pooling and transferring energy cleanly through the stack to terminal rotors pushing into downstream transformers or battery banks.
  - **Monolithic 3D Zero-Flicker Model & Smooth Dynamic Rotation**:
    - Designed to Monolithic 3D standards with a custom 256x256 procedural texture atlas, 0.5px UV inset margin padding, pre-allocated geometry, and PoseStack-transformed normal vectors.
    - Dynamic continuous rotation animation using `(animTicks * spinSpeed) % 360°`, interpolating with game time and partial ticks for silky-smooth 60/120/144+ FPS motion.
    - Detailed geometry featuring a heavy banded timber axle, cast iron hub flange with hex bolts, brass bearing cap, four structural timber spars, steel brackets, angled aerodynamic sail slats, and diagonal cross braces.
  - **Smart Placement & Engineer's Hammer Rotation**:
    - Checks 3x3 clearance upon placement. Placing against an existing rotor automatically aligns the axle axis.
    - Breaking any segment cleanly deconstructs the multiblock and returns exactly 1 `Windmill Rotor` item.
    - Right-clicking with the Grid Engineer's Hammer rotates the axle orientation by 90 degrees if clearance exists.
  - **Diagnostics & Configuration**:
    - Grid Voltmeter integration displays live status: spinning state, air clearance below rotor bottom, real-time FE/t generation (including rain/thunderstorm boosts), and internal energy buffer.
    - Comprehensive configuration in `powergrid.toml` (`windmillMinGroundClearance`, `windmillBaseFePerTick`, `windmillFePerBlockAboveMin`, `windmillMaxFePerTick`, `windmillMaxClearanceChecked`, `windmillCapacity`, `windmillMaxTransfer`).

---

## [1.4.0] - 2026-11-07

### Added
- **Photovoltaic Solar Panel Arrays (Standard, Gold 2x, Diamond 5x)**:
  - **Standard Solar Panel (`powergrid:solar_panel`)**:
    - Low-profile (7px tall) monocrystalline photovoltaic panel with an under-deck inverter chassis, heatsink cooling fins, and corner isolation feet.
    - Generates **1 FE/tick** during full daylight with a **10,000 FE** internal buffer and 100 FE/t export rate.
  - **Gold Solar Panel (`powergrid:gold_solar_panel`)**:
    - Advanced gold-doped photovoltaic generator crafted with Gold Ingots.
    - Generates **2 FE/tick (2x baseline)** during full daylight with a **20,000 FE** internal buffer and 200 FE/t export rate.
    - Features a custom procedural texture atlas with warm golden-amber silicon cells and polished brass/gold frame bezels.
  - **Diamond Solar Panel (`powergrid:diamond_solar_panel`)**:
    - Elite diamond-doped photovoltaic generator crafted with Diamonds.
    - Generates **5 FE/tick (5x baseline)** during full daylight with a **50,000 FE** internal buffer and 500 FE/t export rate.
    - Features a custom procedural texture atlas with brilliant cyan diamond facets and sleek platinum frame bezels.
  - **Seamless Connected Array Architecture**:
    - Placing solar panels adjacent to one another automatically merges inner borders, extending continuous 3D raised silver busbars across cells and shared structural under-deck trusses.
    - Automatically balances stored energy across horizontally connected panel arrays to prevent localized starvation.
    - Pushes accumulated Forge Energy downwards and through side ports into adjacent transformers, accumulators, AWG cables, or wire connectors (`Capabilities.Energy.BLOCK`).
  - **Direct & Upgrade Crafting**:
    - Direct crafting recipes from raw materials for each tier.
    - Convenient upgrade recipes surrounding existing solar panels with Gold Ingots or Diamonds.
- **Architectural & Work-Site Illumination Suite (Migrated from Work Lights)**:
  - **Tripod Work Light (`powergrid:tripod_work_light`)**:
    - 2-block-tall industrial job-site dual halogen floodlight mounted on a safety-yellow telescoping tripod stand with non-slip rubber feet.
    - Right-click anywhere on the stand or light heads to toggle on/off with authentic switch audio (Light Level 15).
  - **Ceiling Shop Light (`powergrid:ceiling_shop_light`)**:
    - 2-block-long commercial dual-tube fluorescent fixture with powder-coated sheet metal housing and electrical punch-outs.
    - Mounts flush to the underside of ceilings; right-click to toggle tubes on/off (Light Level 15).
  - **Wall Floodlight (`powergrid:wall_floodlight`)**:
    - Compact wall-mounted outdoor security floodlight with dual angled floodlight heads, textured reflector lenses, and PIR motion-sensor dome styling.
    - Right-click to toggle on/off (Light Level 15).
  - **Carriage Lantern (`powergrid:carriage_lantern`)**:
    - Traditional coach and carriage exterior wall lantern featuring an ornate cast-iron bracket, pagoda hood, top finial, and glowing incandescent filament bulb.
    - Right-click to toggle on/off (Light Level 15).
- **Tool & Diagnostic Integration**:
  - **Grid Voltmeter Integration**: Right-clicking any solar panel displays live telemetry: panel tier, daylight exposure status, active generation rate, current buffer / capacity, and maximum export limit.
- **Configuration Support**:
  - Added configurable generation rates, capacities, and export limits in `powergrid.toml` (`solarPanelBaseFePerTick`, `solarPanelGoldFePerTick`, `solarPanelDiamondFePerTick`, `solarPanelBaseCapacity`, etc.).

---

## [1.3.1] - 2026-11-07

### Fixed
- **Dynamic Water Wheel Kinetic Rotation Animation**:
  - Implemented the `OPERATING` blockstate property on `WoodenWaterWheelBlock` to provide instantaneous, zero-lag operational state synchronization across all clients within render distance.
  - Upgraded `WoodenWaterWheelRenderer` with continuous game time and partial-tick animation (`(animTicks * spinSpeed) % 360°`), providing ultra-smooth 60/120/144+ FPS rotation when operating in flowing water.
  - Synchronized block entity data tags and packet keys between client and server, ensuring reliable state sync.
  - Added intelligent water flow vector and drop-side detection to drive wheel rotation in the physical direction of incoming currents or waterfall plunges.
  - Dynamically scales rotation speed from 2.0°/tick up to 8.0°/tick based on hydraulic drop height.

---

## [1.3.0] - 2026-11-07

### Added
- **Wooden Water Wheel Kinetic Generator (`powergrid:wooden_water_wheel`)**:
  - **Large 3x3x1 Footprint**: Authentic industrial wooden water wheel multiblock standing 3 blocks high, 3 blocks in diameter, and 1 block wide along its central axle shaft.
  - **Dynamic Water Drop Hydroelectric Generation**:
    - Generates 10 FE/tick per vertical height level of flowing water drop (e.g. 1-block drop = 10 FE/t, 2-block drop = 20 FE/t, 3-block drop = 30 FE/t, with overhead waterfall drops up to 50 FE/t).
    - Requires **flowing water** (`!fluidState.isSource()` or falling/directional flow vectors) to generate torque and spin. Static/still water does not spin the wheel and produces zero torque/FE.
  - **Central Axle Power Output**:
    - Pushes accumulated Forge Energy directly from the center axle hub into adjacent batteries, substation transformers, energy cables, and wire connectors via standard NeoForge `Capabilities.Energy.BLOCK`.
  - **Axle Stacking & Seamless Multi-Wheel Power Transfer**:
    - Water wheels placed adjacent to each other along their axle axis automatically stack into a unified drive shaft.
    - Energy generated by all wheels in the stack is transferred cleanly through the shaft line to the terminal wheel(s), which push the pooled power directly into downstream transformers or batteries without intra-bank sloshing.
  - **Monolithic 3D Zero-Flicker Model**:
    - High-fidelity 3D model designed following modern Monolithic 3D standards with a custom 256x256 procedural texture atlas.
    - Features a heavy banded timber axle, cast iron hub flanges with mounting bolts, 8 radial timber spokes, dual outer rim rings, and 12 angled water buckets.
    - Uses 0.5px half-texel UV inset padding, single multi-faced cuboids, and PoseStack-transformed normal vectors to guarantee zero depth-fighting, flickering, or texture bleeding.
    - Smooth client-side dynamic rotation physics interpolating between ticks.
  - **Smart Placement & Deconstruction**:
    - Checks placement clearance for the entire 3x3x1 vertical plane.
    - Placing against an existing water wheel automatically aligns the axle axis for effortless stacking.
    - Supports `SimpleWaterloggedBlock`, allowing water wheels to be placed directly into rivers, streams, and flumes without wiping out water sources.
    - Breaking any segment of the 3x3 wheel deconstructs the structure cleanly and returns exactly 1 `Wooden Water Wheel` item.
    - Right-clicking with the Grid Engineer's Hammer rotates the axle axis by 90 degrees if clearance allows.
  - **Grid Voltmeter Integration**:
    - Right-clicking any part of the water wheel with the Grid Voltmeter displays live diagnostic telemetry: spinning status, detected water drop height, real-time FE/t generation, and internal buffer levels.
  - **Configuration Options**:
    - Configurable `waterWheelFePerDrop` (default 10 FE/t), `waterWheelMaxDrop` (default 5 levels), `waterWheelCapacity` (default 10,000 FE), and `waterWheelMaxTransfer` (default 2,000 FE/t).

---

## [1.2.1] - 2026-11-06

### Added
- **Refinery Pipe Facade Compatibility for AWG Power Cables**:
  - Full, zero-hard-dependency compatibility with Pipe Facades from the *Industrial Refinery* mod (`refinery:pipe_facade` and `refinery:pipe_facade_hole`).
  - **All 4 AWG Cable Gauges Supported**: Compatible with 12 AWG, 14 AWG, 16 AWG, and 18 AWG Indoor Power Cables (`powergrid:cable_12_gauge`, `powergrid:cable_14_gauge`, `powergrid:cable_16_gauge`, `powergrid:cable_18_gauge`).
  - **Solid & Pass-Through (Hollow) Facade Styles**:
    - **Solid Panels (`refinery:pipe_facade`)**: Completely conceals the cable face inside walls, floors, and ceilings with a 2-pixel architectural slab matching the facade material.
    - **Pass-Through / Hollow Frames (`refinery:pipe_facade_hole`)**: Adds a decorative 2-pixel outer frame while preserving the central opening, allowing connected 4-pixel AWG cable arms to pass cleanly through wall penetrations.
  - **Dynamic In-World Attachment & Swapping**:
    - Right-click any face of an AWG cable block with a facade item to attach it.
    - Right-clicking with a different facade cleanly swaps the facade and returns the previous facade item to the player's inventory (or drops it in the world if inventory is full).
    - Plays the authentic placement sound corresponding to the facade block's material.
  - **Intuitive Removal & Drops**:
    - Sneak-Right-Clicking (Shift + Right-Click) with an empty hand, the **Grid Engineer's Hammer**, or a **Refinery Wrench** removes the targeted facade panel and returns the exact facade item to the player.
    - Breaking the cable block cleanly drops all attached facades in addition to the cable block itself.
  - **Adaptive Collision & Selection Bounding Boxes**:
    - Cable selection and collision shapes automatically expand to include the 2-pixel facade slab on all faced sides, enabling intuitive raycasting, interaction, and physical entity collision.
  - **High-Performance Dynamic Client Rendering**:
    - Dedicated `PowerCableRenderer` and `CableFacadeRenderer` dynamically render 3D facade panels using the target block's own texture atlas sprites and normals, fully compatible with vanilla lighting and LabPBR shaders with zero rendering overhead when no facades are attached.
  - **Wrench Cross-Compatibility**:
    - Right-clicking an AWG cable with a Refinery Wrench (or any wrench tagged under `c:tools/wrench`) now toggles that side's connection, matching the Grid Engineer's Hammer functionality.
  - **Zero Hard Dependency**:
    - Power Grid remains 100% standalone and functional when the Industrial Refinery mod is not installed. Facade integration operates dynamically through soft registry and data component lookups.

---

## [1.2.0] - 2026-11-06

### Added
- **North American High-Pressure Sodium (HPS) Street Lights (`powergrid:street_light`)**:
  - Authentic 4-block municipal street lighting structure featuring a concrete footing pedestal with galvanized anchor bolts, slender tubular metal pole shaft, and curved davit mast arm with a classic teardrop HPS cobrahead luminaire.
  - **Dual-Tier Brightness & Radius**:
    - **Default Standby Mode (Unpowered)**: Emits warm amber standby illumination (Light Level 7, ~7 block radius) immediately upon placement without needing external power.
    - **Active Energized Mode (Powered)**: Emits brilliant high brightness (Light Level 15, full 15-block radius) when energized with 5 FE/t supplied to the base.
  - **Base Energy Capability & Power Consumption**:
    - Concrete footing base implements `Capabilities.Energy.BLOCK` (1,000 FE internal buffer, 32 FE/t max receive rate), accepting connections from Power Cables, Wire Connectors, and adjacent FE energy sources.
    - Active power consumption draws 5 FE/t when illuminated.
  - **Intelligent 4-Block Coordinated Placement**:
    - Placing a single `HPS Street Light` automatically places the complete 4-block vertical assembly (`BASE`, `POLE_LOWER`, `POLE_UPPER`, `HEAD`) facing the player's direction.
    - Breaking any segment cleanly deconstructs the entire light structure and drops a single `HPS Street Light` item, preventing orphaned ghost blocks.
  - **Dusk-to-Dawn Photocell Sensor**:
    - Simulates an authentic NEMA photocell twist-lock sensor on top of the cobrahead fixture.
    - Sneak-Right-Clicking the street light with the Grid Engineer's Hammer toggles between **Automatic (Dusk-to-Dawn)** (only energizes during night/storms when power is available) and **Always Active (24/7)** continuous operation.
    - Normal Right-Clicking with the Grid Engineer's Hammer rotates the fixture's facing direction.
  - **Diagnostic Voltmeter Integration**:
    - Inspecting any part of the street light with the Grid Voltmeter reports real-time energized status, internal buffer level (`X / 1,000 FE`), active power draw (5 FE/t), and photocell sensor state.
  - **HD Models & Ultra-HD 1024x Pack**:
    - Full procedural 32x32 textures and 1024x resource pack support (`(v1.0) 1024x Power Grid.zip`), complete with distinct unpowered and powered sodium-vapor discharge glow states.
  - **Survival Crafting Recipe**:
    - Craftable in crafting tables with Smooth Stone, Redstone, Iron Ingots, and Glowstone.

---

## [1.1.8] - 2026-11-01

### Added
- **Loss-Free Distance Thresholds for Short Runs (`powergrid.toml`)**:
  - Added configurable distance thresholds for all wire tiers below which zero power loss occurs:
    - `lvLossFreeDistance`: Default 8 blocks (LV wire runs $\le$ 8 blocks incur 0% loss).
    - `mvLossFreeDistance`: Default 16 blocks (MV wire runs $\le$ 16 blocks incur 0% loss).
    - `hvLossFreeDistance`: Default 24 blocks (HV wire runs $\le$ 24 blocks incur 0% loss).
  - Short runs between nearby substation equipment, wall-mounted breaker switches, and local machine drops operate at 100% transmission efficiency. For longer runs exceeding the threshold, line resistance applies only to the distance beyond the threshold.

---

## [1.1.7] - 2026-11-01

### Added
- **Configurable Transmission Line Loss (`powergrid.toml`)**:
  - Added a dedicated configuration file (`config/powergrid.toml`) specifying energy transmission loss fractions per block for all wire tiers:
    - `lvLossPerBlock`: Low Voltage (LV) line loss (default: `0.0`, intended production balance: `0.02` / 2.0% per block).
    - `mvLossPerBlock`: Medium Voltage (MV) line loss (default: `0.0`, intended production balance: `0.01` / 1.0% per block).
    - `hvLossPerBlock`: High Voltage (HV) line loss (default: `0.0`, intended production balance: `0.005` / 0.5% per block).
  - All wire losses default to `0.0` (zero line loss / 100% transmission efficiency) for initial testing, debugging, and baseline power grid verification.

---

## [1.1.6] - 2026-11-01

### Fixed
- **Overhead Wire Battery Drain & Bidirectional Sloshing Loop**:
  - Fixed an issue where connecting two Heavy Accumulator banks via overhead utility wires with connectors in `AUTO` mode caused both banks to drain completely to zero.
  - Resolved the underlying resonance catastrophe in `EnergyGridManager`: when both connectors were set to `AUTO`, state-of-charge ratio differences caused full line rate packets (up to 8,192 FE/t) to oscillate between banks, burning all stored energy in wire transmission loss.
  - Added strict storage-to-storage isolation in `EnergyGridManager` so two bidirectional storage nodes (Accumulators or Transformers) in `AUTO` mode never transfer power to each other. Directed transfers can still be performed by explicitly setting one connector to `INPUT` and the other to `OUTPUT`.
- **Zero-Loss Energy Accounting & Distribution Voiding Fix**:
  - Fixed a critical energy void bug in `EnergyGridManager.distributePower` where extracting power upfront from a source would discard and void any unused energy if a connected sink/machine accepted less than the allocated amount.
  - Re-engineered the distribution loop to extract energy from the source incrementally only for the exact amount accepted by each sink plus physical line loss (`sourceConsumed = ceil(inserted / eff)`), ensuring zero energy is voided or lost when machines or batteries near full capacity.

---

## [1.1.5] - 2026-11-01

### Changed
- Mod update

---

## [1.1.5] - 2026-11-01

### Fixed
- **Parasitic Power Draw & Battery Sloshing Loop**:
  - Fixed an issue where power generators would continuously burn fuel and lose energy when connected via AWG power cables to a fully charged Heavy Accumulator bank.
  - Re-architected `PowerCableBlockEntity` energy handling to act as a pure instantaneous conduit: cables now route power directly to valid consumers/sinks in the caller's transaction and return 0 when no sinks require energy, eliminating phantom buffer accumulation and preventing feedback loops into batteries.
  - Added intra-bank protection to `HeavyAccumulatorBlockEntity` and `TransformerBlockEntity` so adjacent storage blocks never push into one another.
  - Updated `CableNetworkRouter.findSinks` to disallow battery-to-battery routing across cables (accumulators discharging into a cable network will only power machines, never other accumulators on the same network).
  - Upgraded `CableNetworkRouter.distributeFromCable` passive extraction to simulate demand in a dry-run transaction first, pulling only the exact energy required by connected sinks and preventing voided energy.

---

## [1.1.3] - 2026-11-01

### Fixed
- **Root Transaction Collision Crash in Cable Network Router**:
  - Fixed a critical server crash (`IllegalStateException: A root transaction of class HeavyAccumulatorBlockEntity is already active on this thread`) that occurred whenever a machine or accumulator ticked next to a power cable.
  - Re-engineered `CableNetworkRouter.routeEnergy` to propagate caller transactions and open nested transactions with `Transaction.open(parentTx)`.
  - Added a transactional `SnapshotJournal` to `PowerCableBlockEntity` for `transferredThisTick`, ensuring cable throughput calculations roll back cleanly during simulations.
  - Removed transaction calls from BFS sink discovery in `findSinks` and added deduplication to avoid redundant insertions.
  - Prevented cable passive extraction from fighting self-ticking energy pushers like Heavy Accumulators and Transformers.

---

## [1.1.2] - 2026-11-01

### Fixed
- **Recipe Parsing Error on Server Startup (Rubber Insulation)**:
  - Registered `c:ingots/rubber` and `c:rubber` common item tags with optional mod item entries (`logistics:core/rubber`, `refinery:rubber`, `techreborn:rubber`).
  - Added `neoforge:conditions` using `neoforge:tag_empty` checks across all four rubber cable recipes (`cable_18_gauge_from_rubber`, `cable_16_gauge_from_rubber`, `cable_14_gauge_from_rubber`, `cable_12_gauge_from_rubber`). This ensures the recipes safely load when a rubber-providing mod is installed and are cleanly skipped without crashing the server's `RegistryDataLoader` if no rubber mod is present.

---

## [1.1.1] - 2026-11-01

### Added
- **Indoor AWG Power Cables (4 Voltage / Gauge Tiers)**:
  - Added modular 6-way indoor power cables for direct machine-to-machine power distribution within buildings, factories, and workshops without requiring overhead pole runs.
  - **18 AWG Cable**: 128 FE/t transfer limit, 128 FE buffer. Requires Copper Wire Strands. Ideal for light workshop appliances, lighting, and low-power automation.
  - **16 AWG Cable**: 512 FE/t transfer limit, 512 FE buffer. Requires Iron Wire Strands. Balanced wiring for midgame machinery and general processing setups.
  - **14 AWG Cable**: 2,048 FE/t transfer limit, 2,048 FE buffer. Requires High-Tensile Steel Wire Strands. Heavy-duty branch cabling for power-hungry machines and smelters.
  - **12 AWG Cable**: 8,192 FE/t transfer limit, 8,192 FE buffer. Requires Gold Wire Strands. High-throughput indoor feeder lines connecting directly to substation transformers and battery banks.
- **Low-Latency Direct Transfer & Minimal Energy Buffering**:
  - Cables maintain a strictly limited 1-tick internal energy buffer (equal to their per-tick transfer rating), ensuring cables transport power instantly without behaving like massive energy storage batteries.
  - Intelligent graph-based routing algorithm (`CableNetworkRouter`) conducts instant breadth-first routing from energy sources to reachable sinks, respecting per-tick throughput limits and path bottlenecks when mixed cable gauges are interconnected.
- **Wire Strands & Material Processing**:
  - Added craftable wire strands: **Copper Wire Strand**, **Iron Wire Strand**, **Steel Wire Strand**, and **Gold Wire Strand**.
  - Added blast furnace recipe for creating Steel Wire Strands from Iron Wire Strands, and added `#c:ingots/steel` tag compatibility.
  - Added flexible recipes for all 4 cable gauges using either Rubber or Dried Kelp as insulation.
- **Precision Tool Interactions**:
  - **Grid Engineer's Hammer**: Right-clicking any face or arm of a power cable toggles the connection on that specific side, enabling manual isolation and neat cable management.
  - **Insulated Wire Cutters**: Right-clicking a cable with wire cutters instantly snips and dismantles the cable, returning the item to your inventory immediately.
  - **Grid Voltmeter**: Right-clicking a power cable displays the gauge, current tick energy throughput, max transfer rate, and internal buffer state.
- **Models, Blockstates & Textures**:
  - Full 3D multipart models for all 4 gauge tiers with custom core boxes, arm connectors, device connection plugs, and 3D item models.
  - Native 16x16 pixel art textures and 1024x1024 Ultra-HD texture pack assets (`(v1.0) 1024x Power Grid.zip`).
  - Full waterloggable support across all cable types.

---

## [1.0.19] - 2026-11-01

### Fixed
- **Substation Transformer Energy Voiding & Back-Feeding Loop**:
  - Fixed a critical energy voiding bug where `TransformerBlockEntity` unconditionally pushed stored energy into attached wire connectors in `AUTO` or `OUTPUT` mode every server tick. This caused the transformer buffer to immediately dump all stored energy into its connector, which routed the power back over the wire to the supplying battery, perpetually draining the battery through line loss while leaving the transformer at 0 FE.
  - `TransformerBlockEntity` now checks the attached wire connector's mode and avoids pushing energy back into connectors unless they are explicitly set to `INPUT` mode (matching `HeavyAccumulatorBlockEntity` behavior).
  - Prevented bidirectional back-feeding in `EnergyGridManager` between Substation Transformers and Heavy Accumulators when both connectors are in `AUTO` mode, allowing batteries to charge substation transformers without oscillation.
  - Synchronized block entity state (`setChanged()`) on both source and sink blocks during grid power distribution so chunk saves retain energy levels.
- **Partial Capacity Insertion**:
  - Fixed adjacent energy transfers in `TransformerBlockEntity`, `HeavyAccumulatorBlockEntity`, and `WireConnectorBlockEntity` using root transaction pre-simulation before buffer extraction, allowing power to cleanly top off machines and buffers whose remaining space is smaller than the maximum transfer rate.

### Changed
- **Substation Transformer Transfer Rate**:
  - Increased `TransformerBlockEntity.MAX_RATE` from 8,192 FE/t to 16,384 FE/t, fully supporting High-Voltage (HV) wire throughput without bottlenecking cross-tier stepping.

---

## [1.0.18] - 2026-11-01

### Fixed
- **Wire Connector & Relay Block Entity Deserialization**:
  - Fixed a critical bug where `WireConnectorBlockEntity` and `WireRelayBlockEntity` constructors called during world loading, chunk loading, or block updates defaulted to `WireType.LV`, causing placed Medium Voltage (MV) and High Voltage (HV) connectors/relays to revert to LV and reject subsequent MV/HV wire connections with `"Node does not support ... wire!"`.
  - Added dynamic blockstate inspection (`getWireTypeFromState`) and NBT tag serialization (`wireType`) so all tiers maintain their proper voltage rating, transfer rates (up to 16,384 FE/t), and buffer sizes across world reloads.
- **Wire Stringing Interaction & Session Safety**:
  - Re-engineered pending wire connection tracking (`PendingWire`) to store the wire tier and game timestamp.
  - Automatically cancels or resets pending runs if a player switches wire coil tiers in their hand or if a pending run is older than 30 seconds (600 ticks), eliminating false "incompatible tier" errors caused by orphaned or stale connections.
  - Added right-click in air (`use`) with any wire coil to cleanly cancel an in-progress wire run with audio and chat confirmation.

---

## [1.0.17] - 2026-11-01

### Removed
- **Power Plant Multiblock Generation Components**:
  - Removed the early 3x3x3 multiblock power plant components (`Power Plant Controller`, `Generator Casing`, `Alternator Copper Coil`, `Turbine Rotor`, and `Industrial Exhaust Stack`) from this mod to focus purely on high-fidelity power transmission, overhead utility lines, and distribution grids.
  - Power generation will be introduced in an upcoming dedicated update/expansion mod.
  - Removed corresponding block entities, capability handlers, loot tables, recipes, models, and textures.

### Changed
- **Tool Updates**:
  - Updated the **Grid Engineer's Hammer** tooltip to reflect rotation and connector mode toggling (Input/Output).
  - Streamlined the **Grid Voltmeter** diagnostic readouts to grid transmission metrics.

---

## [1.0.16] - 2026-11-01

### Changed
- **Block Hardness Halved for Faster Breaking**:
  - Reduced hardness of **Treated Wood Pole** from `2.0f` to `1.0f` (resistance `1.5f`), making it twice as fast to break.
  - Reduced hardness of **Power Pole Crossarm** from `2.0f` to `1.0f` (resistance `1.5f`).
  - Reduced hardness of **Steel Truss Pole** from `4.5f` to `2.0f` (resistance `4.0f`).

### Fixed
- **Block Item Drops in Survival (Loot Tables & Mineable Tags)**:
  - Added complete block loot tables across all Power Grid blocks, ensuring poles, crossarms, connectors, relays, transformers, and accumulators properly drop their corresponding block item when broken in Survival mode.
  - Added Minecraft `#mineable/axe` tags for wooden poles and crossarms, allowing axes to break them swiftly with proper tool speed multipliers.
  - Added Minecraft `#mineable/pickaxe` tags for metallic grid blocks and equipment.

---

## [1.0.15] - 2026-11-01

### Added
- **Custom Creative Mode Tab Icon**:
  - Replaced the placeholder Engineer's Hammer tab icon with a custom **Power Pole & Electrical Lightning Symbol** icon (`grid_icon`).
  - Depicts a detailed treated wooden utility pole equipped with crossarm, ceramic insulators, steel bands, and an energetic electric lightning bolt symbol.
  - Provided in both **32x32** native pixel art and **1024x1024** Ultra-HD resource pack versions.

---

## [1.0.14] - 2026-10-31

### Added
- **Complete Crafting Recipes for JEI & Vanilla Recipe Book**:
  - Added recipe for **Ceramic Insulator** (`ceramic_insulator.json`).
  - Added recipes for all wire connector tiers:
    - **LV Wire Connector** (`lv_wire_connector.json`): Copper ingots + Ceramic Insulator
    - **MV Wire Connector** (`mv_wire_connector.json`): Gold ingots + Ceramic Insulator
    - **HV Wire Connector** (`hv_wire_connector.json`): Iron ingots + Ceramic Insulator
  - Added recipes for all wire relay tiers:
    - **LV Wire Relay** (`lv_wire_relay.json`): Ceramic Insulators
    - **MV Wire Relay** (`mv_wire_relay.json`): Gold ingots + Ceramic Insulators
    - **HV Wire Relay** (`hv_wire_relay.json`): Iron ingots + Ceramic Insulators
  - Added recipes for additional utility and grid equipment:
    - **Wire Cutters** (`wire_cutters.json`)
    - **Breaker Switch** (`breaker_switch.json`)
    - **Steel Truss Pole** (`steel_truss_pole.json`)
    - **Step-Up/Step-Down Transformer** (`transformer.json`)
    - **Heavy Accumulator** (`heavy_accumulator.json`)

---

## [1.0.13] - 2026-10-31

### Added
- **3D Physical Catenary Overhead Powerlines**:
  - Implemented high-performance, segmented 3D powerline rendering (`WireNodeRenderer` & `WireNodeRenderState`) for `WireConnectorBlockEntity`, `WireRelayBlockEntity`, and `BreakerSwitchBlockEntity`.
  - Realistic gravitational catenary sag formula calculated across all spans with dual-perpendicular cross-quad geometry shaded with in-world lighting.
  - Distinct wire tier styling and coloring: LV warm copper (`#B85D19`), MV insulated dark slate (`#4A4E5A`), and HV galvanized steel strand (`#C5CBD3`).
  - Sub-block terminal alignment precisely anchoring wire origins to ceramic insulator tips across all blockstates (`UP`, `DOWN`, `NORTH`, `SOUTH`, `EAST`, `WEST`, `POLE_MOUNT`, and `CROSSARM_END`).
  - Added dedicated `wire_cable.png` textures in 32x32 default resolution and 1024x1024 Ultra-HD resource pack.
- **Directional Connector Modes (`AUTO`, `INPUT`, `OUTPUT`)**:
  - Added configurable connector modes on `WireConnectorBlockEntity` cycled by right-clicking with the `EngineerHammerItem`.
  - `INPUT` (Orange): Dedicated to generators and supply lines — extracts from the attached machine and pushes directly to the wire grid.
  - `OUTPUT` (Blue): Dedicated to machines and energy storage — receives from the wire grid and feeds into the attached machine without back-feeding.
  - `AUTO` (Green / Default): Smart bidirectional sensing delivering energy only to hungry sinks.
  - Sound effect and actionbar notification on mode cycle.

### Fixed
- **Wire Relay Passthrough & Grid Energy Routing**:
  - Implemented `EnergyGridManager` with a unified BFS pathfinding engine. Wire Relays now seamlessly route power as pass-through waypoints between sources and sinks.
  - Closed Breaker Switches freely pass energy, while open switches cleanly sever the circuit.
  - Eliminated intermediate buffer sloshing and infinite loop energy depletion by extracting directly from source machines and delivering to destination machines in unified transactions with line loss applied once per transmission path.
  - Prevented batteries (`HeavyAccumulatorBlockEntity`) from force-feeding into wire connectors in `OUTPUT` mode or `AUTO` mode without downstream demand.
- **Orphaned Wire Connections & Wire Refunds on Destruction**:
  - Implemented `preRemoveSideEffects` on `WireConnectorBlockEntity`, `WireRelayBlockEntity`, and `BreakerSwitchBlockEntity`.
  - Breaking any connected node immediately notifies and severs connections on all remote nodes, triggers block updates, and refunds the respective wire coils (`lv_wire_coil`, `mv_wire_coil`, `hv_wire_coil`) as ground item drops in Survival mode.
  - `WireCuttersItem` now refunds wire coils when cutting active wire spans.
- **Client Synchronization**:
  - Implemented `getUpdatePacket()` and `getUpdateTag()` on all `IWireNode` block entities, ensuring client instances have live, synchronized wire connection and mode data.

---

## [1.0.12] - 2026-10-30

### Fixed
- **Wire Coil Attachment False "Incompatible Wire Tier" Bug**:
  - Resolved an issue in `WireCoilItem` where clicking a connector or relay for the first time passed the node's own position as `otherPos` into `canConnectWire(WireType, BlockPos)`. Because `canConnectWire` verified `!otherPos.equals(this.worldPosition)`, it rejected the node on the first click with "Node does not support X wire!".
  - Added `isWireTypeSupported(WireType)` and `canTakeMoreConnections()` to `IWireNode` to separate single-node tier and capacity checks from two-node distance and loop-connection checks.
  - Implemented `getMaxConnections()` on `WireConnectorBlockEntity` (4 lines), `WireRelayBlockEntity` (8 lines), and `BreakerSwitchBlockEntity` (2 lines).
  - Breaker Switches now universally support in-line disconnect switching across all three wire tiers (LV, MV, HV).

---

## [1.0.9] - 2026-10-30

### Fixed
- **Crossarm-End Wire Connector & Relay Vertical Alignment**:
  - Corrected horizontal model rotation coordinate transformation for crossarm end attachments across LV, MV, and HV Wire Connectors and Wire Relays.
  - Sized the crossarm-end base plate to precisely $6.4 \times 6.4$ pixels ($X/Z \in [4.8, 11.2]$, $Y \in [9.8, 16.2]$) to fit flush onto the $6 \times 6$ timber crossarm beam and its steel end bracket.
  - Centered all insulators, bushings, wire clamps, and terminals at exact beam centerline height $Y=13.0$ with zero vertical droop or overhang.
  - Refined VoxelShape collision and selection bounding boxes across all horizontal facings (`NORTH`, `SOUTH`, `WEST`, `EAST`) to tightly bound the elevated $Y=13.0$ connector geometry ($Y \in [9.5, 16.5]$).

---

## [1.0.7] - 2026-10-28

### Added
- **Top-of-Pole Crossarm Mounting (`on_pole`)**:
  - Implemented `ON_POLE` state in `PoleCrossarmBlock` when placed directly on top of a pole (`TreatedWoodPoleBlock` or `SteelTrussPoleBlock`).
  - Added dedicated 3D steel top-cap mounting collar (`pole_crossarm_top_cap.json`) featuring a heavy flat steel cap plate resting directly on the top surface of the pole at $Y=0.0$, downward steel clasp flanges wrapping the pole top ($Y=-3.0..0.0$), a central vertical structural riser ($Y=1.2..10.0$), and diagonal angle gussets bracing the horizontal crossarm timber beam.
  - Dynamically updates VoxelShapes to include the riser column down to $Y=0.0$.

- **Connector & Relay Crossarm-End Alignment (`crossarm_end`)**:
  - Added `CROSSARM_END` state to all 6 Wire Connector and Wire Relay blocks (`lv/mv/hv_wire_connector`, `lv/mv/hv_wire_relay`).
  - Added specialized 3D models (`_crossarm_end`) vertically aligned to $Y=13.0$ matching the exact center axis of the horizontal crossarm timber beam ($Y=10.0..16.0$).
  - Eliminates the 5px drooping vertical displacement when wire connectors and relays are placed on the outer end face of a crossarm.

- **Connector & Relay Pole / Crossarm Standoff Brackets (`pole_mount`)**:
  - Added `POLE_MOUNT` state to all 6 Wire Connector and Wire Relay blocks when attached to the side face of a vertical pole or side face of a crossarm beam.
  - Added specialized 3D models (`_pole_mount`) featuring heavy structural steel standoff channel brackets that extend 5px backwards across the block boundary into the pole/crossarm timber face, eliminating the floating air gap.

### Fixed
- **Side-of-Pole Crossarm Gap**:
  - Re-engineered `pole_crossarm_bracket_neg_x.json`, `_pos_x.json`, `_neg_z.json`, and `_pos_z.json` models to extend full timber beam extensions to the pole face (4.5px back) and wrap steel collar straps around the pole to its center ($X/Z=\pm8.0$), locking the crossarm flush to the pole with zero air gap.
  - Fixed neighbor orientation detection in `PoleCrossarmBlock.calculateState` to correctly recognize adjacent poles and prevent erroneous end-caps facing into pole joints.

---

## [1.0.5] - 2026-10-28

### Changed
- **Heavy Duty Safety Switch / Disconnect Enclosure Redesign**:
  - Replaced the solid 16x16 cube with an authentic wall-mounted industrial disconnect safety switch modeled directly after commercial Siemens/Square-D heavy-duty enclosures.
  - **3D Sheet Metal Cabinet Geometry**: Features a slim wall-mounted NEMA enclosure with beveled overlapping hinged front door, stamped raised center panel, left barrel hinges, padlockable door latch clasp, and top/bottom threaded conduit hubs.
  - **Animated Red Disconnect Lever**: Equipped with a side-mounted steel pivot linkage and bright red molded safety grip handle that physically snaps between the **UP** position (Circuit Closed / Power ON) and the **DOWN** position (Circuit Open / Power OFF) when toggled or triggered by Redstone.
  - **Industrial Placard & Indicator Art**: High-visibility front door safety placard with bold ON/OFF status badges, "HEAVY DUTY SAFETY SWITCH", 600V rating, and high-voltage DANGER arc flash warning banner. Available in both 32x32 default pixel art and 1024x1024 photorealistic Ultra-HD.
  - **Wall-Mount VoxelShapes**: Aligned directional collision boxes for North, South, East, and West facings so the safety switch sits flush against walls.
  - **Intelligent Placement**: Automatically mounts against the clicked wall face or faces the player if placed on floor or ceiling.

### Fixed
- **Resource Pack Version Compatibility (Minecraft 26.3)**:
  - Updated `pack.mcmeta` with `pack_format: 84`, `supported_formats: [15, 255]`, `min_format: 15`, and `max_format: 255` matching your other working 1024x resource packs, resolving the "Made for an older version of Minecraft" warning.

---

## [1.0.3] - 2026-10-27

### Added
- **High-Fidelity 32x32 Default Textures**:
  - Upgraded all 30 block textures and 11 item textures to crisp, detailed 32x32 pixel art with creosote grain, metallic specular highlights, glazed ceramic porcelain sheens, and analog meter dials.
- **1024x1024 Ultra-HD Resource Pack**:
  - Generated and packaged `(v1.0) 1024x Power Grid.zip` in `resourcepacks/` containing ultra-HD 1024x1024 textures, format 61 `pack.mcmeta`, and custom high-voltage transmission artwork `pack.png`.
  - Automatically installed into the active CurseForge client profile (`Andrew/resourcepacks`).
- **Dynamic Crossarm Extension**:
  - Right-clicking an active `PoleCrossarmBlock` with a crossarm item automatically expands the horizontal beam outward to the next block space without risk of falling.
- **Tower Auto-Stacking**:
  - Right-clicking `SteelTrussPoleBlock` with a truss pole in hand automatically ascends the column and stacks a new tower block at the top.

### Changed
- **Round Wooden Telephone Pole (Image 1)**:
  - Replaced the full 16x16 cube with an 8-sided cylindrical wooden utility pole (centered 7x16x7 column).
  - Added galvanized steel reinforcement bands at dual heights, alternating iron climbing rungs (spikes), and an end-grain top weather cap.
- **Expanding Power Pole Crossarm (Image 2)**:
  - Replaced full-cube crossarms with a rounded 6x6 horizontal timber beam.
  - Implemented multi-part horizontal connection logic: detects adjacent `TreatedWoodPoleBlock` and wraps a heavy forged steel collar around the pole, while connecting seamlessly through neighboring crossarms to build multi-block horizontal crossarm assemblies.
  - Positioned top beam surface at Y=16.0 so Wire Relays and Connectors placed on top sit flush with zero gap.
- **3D Inventory and GUI Rendering**:
  - Configured 3D isometric display transforms for poles, crossarms, wire relays, and connectors so they render as true 3D models in hand, hotbar, and inventory screens.

### Fixed
- **Hollow / Transparent Broken Wire Relays (Image 3)**:
  - Eliminated the OpenGL face-culling and transparent see-through ground glitch by replacing flat textures on solid cubes with full 3D element JSON geometry.
  - LV, MV, and HV wire relays and connectors now feature distinct 3D geometry: cast iron mounting footplates, tiered ceramic insulator bells (single, double, and triple-bell stacks), and conductor wire saddle clamps.
- **Directional VoxelShape Inversion**:
  - Corrected bounding boxes for `WireRelayBlock` and `WireConnectorBlock` across all 6 faces (`UP`, `DOWN`, `NORTH`, `SOUTH`, `WEST`, `EAST`) so raycasting and block collisions align with model geometry.
- **Pole Stacking State Resolution**:
  - Corrected `TreatedWoodPoleBlock` right-click placement to resolve neighbor connectivity against the new top coordinate instead of the clicked base block.

---

## [1.0.1] - 2026-10-26

### Fixed
- **Missing UI Icons & Textures (Purple/Black Checkered Blocks)**:
  - Added complete Minecraft 26.3 item definition files in `assets/powergrid/items/` for all 17 blocks and 11 items.
  - Added full item models (`models/item/`), block models (`models/block/`), and multi-state blockstates (`blockstates/`).
  - Added 16x16 pixel art textures for all utility poles, steel lattice towers, crossarms, wire connectors, relays, transformers, accumulators, multiblock components, tools, and wire coils.
  - Updated `build.bat` to automatically sync the compiled jar to the active CurseForge client profile (`Andrew/mods`).

---

## [1.0.0] - 2026-10-25

### Added
- **Initial Release for Minecraft 26.3 & NeoForge 26.3.0.48-beta**:
  - Immersive Engineering-inspired electrical distribution, overhead power poles, and multiblock generation.
- **Three-Tier Voltage Grid Architecture**:
  - **Low Voltage (LV)**: 400 FE/t, 16-block max span (Copper wire).
  - **Medium Voltage (MV)**: 2,048 FE/t, 32-block max span (Electrum wire).
  - **High Voltage (HV)**: 16,384 FE/t, 64-block max span (Steel-core aluminum cable).
- **Overhead Infrastructure**:
  - `TreatedWoodPoleBlock`: Vertical stackable utility poles.
  - `SteelTrussPoleBlock`: Heavy industrial lattice towers for high-voltage runs.
  - `PoleCrossarmBlock`: Horizontal crossarm mounts for connectors and insulators.
- **Wire Connectors & Relays**:
  - `WireConnectorBlock` (LV, MV, HV): Interfaces directly with external block energy storage (`Capabilities.Energy.BLOCK`).
  - `WireRelayBlock` (LV, MV, HV): Pass-through insulators for routing wire lines around obstacles and across long spans without block energy interfacing.
- **Substation Gear**:
  - `TransformerBlock`: Step-up and step-down substation transformer connecting voltage tiers.
  - `BreakerSwitchBlock`: Redstone-controllable or manually switched in-line circuit breaker.
  - `HeavyAccumulatorBlock`: Heavy industrial 4,000,000 FE capacitor bank.
- **Multiblock Power Plant (3x3x3)**:
  - Formed using the Grid Engineer's Hammer on the `PowerPlantControllerBlock`.
  - Realistic industrial generation producing 4,096 FE/t with chimney smoke particles and machine hum.
- **Tools & Diagnostics**:
  - `EngineerHammerItem`: Toggles multiblock formation and rotates directional grid blocks.
  - `VoltmeterItem`: Live diagnostic reader for stored energy, throughput, and connected wire paths.
  - `WireCuttersItem`: Safely severs wire connections from any connector or relay.
  - `WireCoilItem` (LV, MV, HV): Click-to-connect tool for hanging overhead catenary wires.

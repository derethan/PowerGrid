# Changelog — Power Grid

All notable changes to the **Power Grid** mod are documented in this file.

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

# CurseForge Upload Template & Metadata Guide: Power Grid

This document contains everything needed to publish and update **Power Grid** on CurseForge. You can copy-paste each section directly into the CurseForge Project Creation and File Upload forms.

---

## 1. CurseForge Project Submission Fields

### Project Name
`Power Grid`

### Short Summary / Tagline (Max 255 characters)
> Heavy industrial electricity distribution, overhead power poles, high-voltage transmission lines, substations, and multiblock power plants for modern Minecraft!

### Primary Category
- **Technology**

### Secondary Categories
- **Energy, Fluid, and Item Transport**
- **Processing**
- **Automation**

---

## 2. Project Description (Markdown / Rich Text)
*(Copy everything below this line into the "Description" box on CurseForge)*

---

# ⚡ Power Grid
**Heavy Overhead Electricity Distribution • High-Voltage Catenary Wires • Substations • 3x3x3 Multiblock Power Plants**

*Built natively for Minecraft 26.3 on NeoForge.*

![Power Grid Logo](logo.png)

---

## 🔌 Overview
**Power Grid** brings authentic electrical engineering back to Minecraft, deeply inspired by the beloved mechanics of *Immersive Engineering*. Say goodbye to invisible magical power cables buried underground — **Power Grid** requires you to plan, run, and maintain real overhead transmission lines across poles, towers, substations, and factories!

From running early copper Low-Voltage lines to your first workshop machines, to stepping up power with Substation Transformers and stringing heavy steel-core High-Voltage cables across entire mountain ranges to a massive 3x3x3 Multiblock Power Plant, Power Grid makes power transmission visually spectacular and engineeringly rewarding.

---

## ⚡ Core Features

### 1. Three-Tier Voltage Grid Architecture
- **Low Voltage (LV)**: 400 FE/t throughput, 16-block maximum span. Crafted from copper. Ideal for local workshops, lighting, and standalone machines.
- **Medium Voltage (MV)**: 2,048 FE/t throughput, 32-block maximum span. Crafted from electrum. Perfect for factory sub-mains and midgame industrial machinery.
- **High Voltage (HV)**: 16,384 FE/t throughput, 64-block maximum span. Heavy steel-core cables engineered for long-distance cross-country grid transmission.

### 2. Distance Resistance & Transmission Losses
- Just like real-world power grids, longer wire spans encounter electrical resistance.
- Low voltage suffers a 2.0% loss per block, making regional runs inefficient.
- High voltage lines feature only 0.5% loss per block, incentivizing engineers to construct realistic regional substations with step-up transformers!

### 3. Utility Poles & Transmission Towers
- **Treated Wood Utility Poles**: Authentic 8-sided cylindrical wooden telephone poles with steel bands and climbing rungs. Right-clicking with a pole item automatically stacks the pole upwards!
- **Steel Truss Lattice Towers**: Massive, blast-resistant high-voltage transmission towers for overland cable runs. Supports right-click auto-stacking.
- **Pole Crossarms**: Modular rounded horizontal timber crossarms that automatically clamp to adjacent poles with forged iron collars and connect seamlessly across neighboring crossarms. Right-clicking automatically extends the beam horizontally!

### 4. Connectors, Relays, & Substation Gear
- **32x32 Textures & 1024x1024 Ultra-HD Pack**: Default 32x32 textures with creosote grain and glazed porcelain sheens, plus an included 1024x1024 Ultra-HD resource pack.
- **3D Element Models**: True 3D geometry for wire relays and connectors with multi-tiered porcelain insulator bells and conductor saddle clamps.
- **Wire Connectors**: Placed directly on machines, batteries, or generators. Automatically interfaces with NeoForge's standard `Capabilities.Energy.BLOCK` to push or pull line energy.
- **Wire Relays**: High-density pass-through insulators designed to turn corners and route branches without tapping into a block's internal buffer.
- **Substation Transformers**: High-efficiency (98%) step-up and step-down transformers connecting LV ⇄ MV and MV ⇄ HV networks.
- **Heavy Duty Safety Disconnect Switches**: Wall-mounted industrial disconnect safety switches modeled after commercial Siemens/Square-D enclosures. Features a throw switch with an animated red safety handle that physically flips between ON (UP) and OFF (DOWN) when toggled or driven by Redstone.
- **Heavy Accumulator Banks**: Industrial multi-cell capacitor storage units buffering up to **4,000,000 FE** each with rapid charge/discharge capabilities.

### 5. Multiblock Heavy Power Plant (3x3x3)
- Construct a true heavy industrial power plant using **Generator Casings**, **Alternator Coils**, **Turbine Rotors**, and **Industrial Exhaust Stacks**.
- Strike the front controller with the **Grid Engineer's Hammer** to assemble the multiblock.
- Produces **4,096 FE/t** of continuous industrial electricity, complete with animated smoke plumes and realistic mechanical hums!

### 6. Specialized Engineering Tools
- **Wire Coils (LV, MV, HV)**: Right-click the starting terminal, then right-click the destination terminal to string physical catenary wires.
- **Grid Voltmeter**: Live diagnostic multimeter reporting stored FE, throughput, voltage tier, and all connected terminal coordinates.
- **Grid Engineer's Hammer**: Validates and commissions multiblock power plants and rotates directional grid gear.
- **Insulated Wire Cutters**: Safely and cleanly disconnects all attached wire lines from any connector or relay.

---

## 🛠️ Mod Compatibility & Integration
- **Universal Forge Energy (FE)**: Built natively on NeoForge's Transfer API. Fully compatible with any mod supporting standard FE capabilities (`Capabilities.Energy.BLOCK`).
- **Industrial Refinery Synergy**: Connect an HV Wire Connector directly to refinery diesel generators or steam boilers to power your entire base from a central power station.
- **Logistics Mod Synergy**: Feed automation sorters, item pipes, and craft tables directly from overhead wire drop lines.

---

## 📦 Recent Release Notes (v1.0.7)
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
- **Side-of-Pole Crossarm Gap Fix**:
  - Re-engineered `pole_crossarm_bracket_neg_x.json`, `_pos_x.json`, `_neg_z.json`, and `_pos_z.json` models to extend full timber beam extensions to the pole face (4.5px back) and wrap steel collar straps around the pole to its center ($X/Z=\pm8.0$), locking the crossarm flush to the pole with zero air gap.
  - Fixed neighbor orientation detection in `PoleCrossarmBlock.calculateState` to correctly recognize adjacent poles and prevent erroneous end-caps facing into pole joints.

---

## 📦 Previous Release Notes (v1.0.5)
- **Heavy Duty Safety Switch / Disconnect Enclosure Redesign**:
  - Replaced the solid 16x16 cube with an authentic wall-mounted industrial disconnect safety switch modeled directly after commercial Siemens/Square-D heavy-duty enclosures.
  - **3D Sheet Metal Cabinet Geometry**: Features a slim wall-mounted NEMA enclosure with beveled overlapping hinged front door, stamped raised center panel, left barrel hinges, padlockable door latch clasp, and top/bottom threaded conduit hubs.
  - **Animated Red Disconnect Lever**: Equipped with a side-mounted steel pivot linkage and bright red molded safety grip handle that physically snaps between the **UP** position (Circuit Closed / Power ON) and the **DOWN** position (Circuit Open / Power OFF) when toggled or triggered by Redstone.
  - **Industrial Placard & Indicator Art**: High-visibility front door safety placard with bold ON/OFF status badges, "HEAVY DUTY SAFETY SWITCH", 600V rating, and high-voltage DANGER arc flash warning banner. Available in both 32x32 default pixel art and 1024x1024 photorealistic Ultra-HD.
  - **Wall-Mount VoxelShapes**: Aligned directional collision boxes for North, South, East, and West facings so the safety switch sits flush against walls.
  - **Intelligent Placement**: Automatically mounts against the clicked wall face or faces the player if placed on floor or ceiling.
- **Resource Pack Version Compatibility (Minecraft 26.3)**:
  - Updated `(v1.0) 1024x Power Grid.zip` `pack.mcmeta` with `pack_format: 84`, `supported_formats: [15, 255]`, `min_format: 15`, and `max_format: 255` matching your other working 1024x resource packs, resolving the "Made for an older version of Minecraft" warning.

---

## 📦 Previous Release Notes (v1.0.3)
- **32x32 Default Textures**: Upgraded all block and item textures to rich 32x32 pixel art with creosote grain, metallic specular highlights, and ceramic porcelain sheens.
- **1024x1024 Ultra-HD Resource Pack**: Added `(v1.0) 1024x Power Grid.zip` in `resourcepacks/` (and synced to CurseForge profile) for players who want photorealistic ultra-HD graphics.
- **Round Wooden Telephone Poles**: Replaced solid cubes with authentic 8-sided cylindrical telephone poles with steel bands and climbing steps. Supports right-click auto-stacking!
- **Expanding Power Pole Crossarms**: Modular rounded timber crossarms that automatically clamp to adjacent poles with forged iron collars and seamlessly extend horizontally across neighboring crossarms. Supports right-click auto-extension!
- **3D Wire Relays & Connectors**: Replaced broken flat transparent cube models with true 3D element models featuring cast iron baseplates, multi-tier porcelain insulator bells, and conductor wire saddles.
- **VoxelShape Alignment**: Corrected directional collision and interaction hitboxes across all 6 faces for relays and connectors.

---

## 📜 License
Power Grid is released under the **MIT License**. You are free to include it in any modpack, server, or custom collection!

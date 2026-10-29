# Power Grid — Complete Engineering Wiki & Operator's Guide
*A comprehensive guide to high-voltage power distribution, overhead utility infrastructure, substations, and multiblock generation for Minecraft 26.3 NeoForge.*

![Power Grid Logo](logo.png)

---

## Table of Contents
1. [Overview & Philosophy](#1-overview--philosophy)
2. [Essential Engineering Tools](#2-essential-engineering-tools)
   - [Grid Engineer's Hammer](#grid-engineers-hammer)
   - [Grid Voltmeter](#grid-voltmeter)
   - [Insulated Wire Cutters](#insulated-wire-cutters)
   - [Overhead Wire Coils (LV, MV, HV)](#overhead-wire-coils-lv-mv-hv)
3. [The Three-Tier Transmission Grid](#3-the-three-tier-transmission-grid)
   - [Low Voltage (LV)](#low-voltage-lv)
   - [Medium Voltage (MV)](#medium-voltage-mv)
   - [High Voltage (HV)](#high-voltage-hv)
   - [Line Resistance & Transmission Loss](#line-resistance--transmission-loss)
4. [Overhead Poles & Infrastructure](#4-overhead-poles--infrastructure)
   - [Treated Wood Utility Poles](#treated-wood-utility-poles)
   - [Steel Truss Lattice Towers](#steel-truss-lattice-towers)
   - [Pole Crossarms & Insulator Mounts](#pole-crossarms--insulator-mounts)
5. [Connectors, Relays, & Substations](#5-connectors-relays--substations)
   - [Wire Connectors (Block Interface)](#wire-connectors-block-interface)
   - [Wire Relays (Pass-Through Routing)](#wire-relays-pass-through-routing)
   - [Substation Transformers (Step-Up / Step-Down)](#substation-transformers-step-up--step-down)
   - [High-Voltage Breaker Switches](#high-voltage-breaker-switches)
   - [Heavy Accumulator Banks](#heavy-accumulator-banks)
6. [Heavy Multiblock Power Plant (3x3x3)](#6-heavy-multiblock-power-plant-3x3x3)
   - [Structural Requirements & Bill of Materials](#structural-requirements--bill-of-materials)
   - [Layer-by-Layer Construction Diagram](#layer-by-layer-construction-diagram)
   - [Commissioning with the Engineer's Hammer](#commissioning-with-the-engineers-hammer)
   - [Thermodynamic Generation & Operational Behavior](#thermodynamic-generation--operational-behavior)
7. [Comprehensive Recipe Codex](#7-comprehensive-recipe-codex)
8. [Cross-Mod Interoperability & Capabilities](#8-cross-mod-interoperability--capabilities)
9. [Frequently Asked Questions (FAQ) & Troubleshooting](#9-frequently-asked-questions-faq--troubleshooting)

---

## 1. Overview & Philosophy

In modern modded Minecraft, electricity has often been reduced to magic single-block cables hidden beneath flooring. **Power Grid** brings back the romance, realism, and spatial puzzle of genuine electrical infrastructure, deeply inspired by the golden standard set by *Immersive Engineering*.

- **Physical Overhead Lines**: Run catenary copper, electrum, and steel-core wires strung between utility poles, rooftops, and substation yards.
- **Voltage Tier Progression**: Power cannot simply jump arbitrary distances without consequences. Low voltages suffer high transmission loss, demanding step-up transformation to High Voltage (HV) for long-distance cross-country spans.
- **Industrial Scale**: Build true 3x3x3 multiblock power plants that shake the ground, belch industrial chimney exhaust, and generate enough continuous wattage to energize your entire technological complex.
- **Native NeoForge Transfer API**: Built from the ground up for **Minecraft 26.3** and **NeoForge 26.3.0.48-beta**, natively exposing standard `Capabilities.Energy.BLOCK` for zero-lag, instant compatibility with mods like *Industrial Refinery*, *Logistics*, and all Forge Energy (FE) machines.

---

## 2. Essential Engineering Tools

Before building an electrical network, an engineer must craft their diagnostic and installation kit.

### Grid Engineer's Hammer
The quintessential tool of the trade.
- **Block Orientation**: Right-click on directional grid components (transformers, crossarms, breakers) to cycle their horizontal facing clockwise.
- **Multiblock Assembly**: Strike the front face of a completed 3x3x3 Power Plant structure at the `PowerPlantControllerBlock` to validate and commission the multiblock into an active generator. Striking an assembled plant safely decommissions it.
- **Durability**: Indestructible hand tool.

### Grid Voltmeter
A precision electronic multimeter for live grid diagnostics.
- Right-click any **Wire Connector** or **Wire Relay** to display:
  - Voltage rating (LV, MV, or HV)
  - Number of connected lines
  - Destination coordinates and span distance of each connected wire
- Right-click any **Battery / Accumulator / Machine** to inspect:
  - Stored Forge Energy (FE) and maximum buffer capacity
- Right-click an assembled **Power Plant** to check running status and burn-cycle timers.

### Insulated Wire Cutters
High-leverage ceramic-insulated wire snips.
- Right-click any **Wire Connector** or **Wire Relay** to immediately sever all attached wire lines, dropping the consumed wire coils back into your hands or onto the ground.

### Overhead Wire Coils (LV, MV, HV)
The lifeblood of the power grid.
- **How to String Wires**:
  1. Hold a wire coil in your main hand.
  2. Right-click on the first terminal (Connector, Relay, or Breaker). A subtle audio knot click confirms the starting node is anchored.
  3. Walk to your target destination terminal and right-click it.
  4. The wire line instantly attaches, playing a resonant metallic chime!
- **Canceling an Open Run**: Shift-right-click empty air or any non-terminal block with the coil to cancel the starting node anchor.

---

## 3. The Three-Tier Transmission Grid

Power Grid enforces three strict voltage tiers. Terminal blocks (connectors and relays) will **refuse** to connect to mismatched wire tiers unless routed through a **Substation Transformer**.

| Voltage Tier | Conductor Material | Max Throughput | Max Span Distance | Resistance Loss Rate | Recommended Application |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **LV (Low Voltage)** | Annealed Copper | **400 FE/t** | **16 blocks** | 2.0% per block | Workshops, early machinery, indoor lighting |
| **MV (Medium Voltage)** | Electrum (Au-Ag) | **2,048 FE/t** | **32 blocks** | 1.0% per block | Factory sub-mains, industrial smelters |
| **HV (High Voltage)** | Steel-Core Aluminum | **16,384 FE/t** | **64 blocks** | 0.5% per block | Cross-country overland grids, power plant export |

### Line Resistance & Transmission Loss
Long overhead spans naturally encounter physical electrical resistance. When power travels between two terminals, the net energy delivered to the receiving node equals:

$$\text{Net Received} = \text{Transmitted FE} \times \left(1 - (\text{Distance} \times \text{Loss Rate})\right)$$

*Example*: Sending 1,000 FE over 20 blocks of MV wire (1% loss per block = 20% loss) delivers 800 FE. Stepping up to HV wire (0.5% loss per block = 10% loss) preserves 900 FE!

---

## 4. Overhead Poles & Infrastructure

### Treated Wood Utility Poles
- Authentic **8-sided cylindrical wooden telephone poles** treated with deep creosote oil.
- Features dual galvanized steel reinforcement collars, alternating iron climbing rungs (spikes), and an end-grain top weather-protection cap.
- **Auto-Stacking**: Right-clicking any section of an existing pole with a pole item in hand automatically climbs to the top of the column and places the new pole, making tall utility pole construction fast and safe.

### Steel Truss Lattice Towers
- Heavy industrial galvanized steel truss columns designed for high-voltage transmission lines.
- Features 4 corner angle-iron posts, diagonal cross-lacing struts, and heavy gusset plates.
- **Tower Auto-Stacking**: Right-clicking any tower block with another truss pole in hand automatically stacks the tower upwards.
- Extremely blast-resistant and weather-hardened.

### Pole Crossarms & Insulator Mounts
- **Modular Rounded Timber Crossarm**: A rounded 6x6 horizontal creosote timber beam designed to attach directly to telephone poles.
- **Intelligent Side & Top Pole Attachment**:
  - **Side-Mount**: When placed against the side of a `TreatedWoodPoleBlock`, timber beams reach directly to the pole surface with heavy wrap-around forged steel collar straps and diagonal support gussets with zero air gap.
  - **Top-Mount (Flat Cap)**: When placed directly on top of a pole, it sits flat on the pole top with a heavy galvanized steel saddle plate, downward pole collar clasps, vertical riser column, and diagonal timber beam braces.
- **Horizontal Expansion**: Seamlessly connects to adjacent crossarms on either side, allowing builders to construct wide 3-block, 5-block, or multi-circuit high-voltage crossarm structures.
- **Auto-Extension**: Right-clicking an existing crossarm with a crossarm item automatically places the next segment extending horizontally outward.
- **Flush Insulator Mounting**: Top beam surface is positioned at Y=16.0 so Wire Relays and Connectors sit flush on top with zero gap.

---

## 5. Connectors, Relays, & Substations

### Visual Quality & 3D Element Models
- **32x32 High-Fidelity Default**: All blocks and items feature handcrafted 32x32 pixel art textures with creosote grain, metallic specular highlights, and glazed ceramic sheens.
- **1024x1024 Ultra-HD Resource Pack**: Includes a dedicated `(v1.0) 1024x Power Grid.zip` resource pack featuring photorealistic ultra-high-definition textures for all grid equipment.
- **Full 3D Element Geometry & Precision Alignment**:
  - **Crossarm-End Alignment**: When mounted on the end face of a horizontal crossarm, connectors and relays automatically center along the crossarm's beam axis ($Y=13.0$), eliminating any vertical sagging or drooping below the wood beam.
  - **Pole & Crossarm Standoff Brackets**: When placed on the side of a round wooden pole or side of a crossarm, heavy structural steel standoff channel brackets extend 5px backwards across the block boundary to clasp the timber flush, eliminating floating gaps.
  - **Flush Surface Mounting**: Mounted flush on full blocks, ceilings, and directly on top of poles and crossarm beams.

### Wire Connectors (Block Interface)
- Mounted on the face of any machine, power plant, or battery.
- **Directional Energy Link**: The rear face of the connector automatically searches the block it is attached to for standard NeoForge energy capabilities (`Capabilities.Energy.BLOCK`).
- Automatically balances power: pulls excess power from generators and injects stored line power into consumers.
- Supports up to **4 simultaneous wire attachments**.

### Wire Relays (Pass-Through Routing)
- Multi-tier ceramic standoff insulators that **do not** interact with the block behind them:
  - **LV Relay**: Single porcelain ribbed bell with copper conductor clamp.
  - **MV Relay**: Dual-tiered terracotta porcelain bells with electrum saddle clamp.
  - **HV Relay**: Heavy three-tiered slate-blue porcelain suspension bells with heavy steel suspension clamp.
- Designed strictly to bridge spans, make sharp 90-degree turns around buildings, or distribute branches across open yards.
- High-capacity terminals supporting up to **8 simultaneous wire attachments**.

### Substation Transformers (Step-Up / Step-Down)
- The heart of any regional electrical grid.
- Allows seamless conversion between adjacent tiers:
  - **LV to MV** (and vice versa)
  - **MV to HV** (and vice versa)
- Built with internal laminated silicon-steel cores providing a **98% power conversion efficiency**.
- Equipped with a 64,000 FE high-speed balancing buffer.

### Heavy-Duty Safety Disconnect Switches (Breaker Switch)
- Authentic **wall-mounted industrial disconnect safety switch** modeled after commercial Siemens/Square-D heavy-duty enclosures.
- **3D Sheet Metal Cabinet**: Slim wall-mounted enclosure featuring a beveled hinged front door, stamped raised panel, left barrel hinges, padlockable latch clasp, and top/bottom threaded conduit hubs.
- **Animated Red Disconnect Lever**: Features a side-mounted steel pivot arm with a bright red molded safety grip handle. When toggled, the handle physically snaps between **UP** (Circuit Closed / Power ON) and **DOWN** (Circuit Open / Power OFF).
- **Manual & Redstone Operation**: Right-click to throw the disconnect switch with a resonant industrial metallic clack, or connect a redstone signal to automate circuit cutoffs during grid surges or emergencies.
- Acts as a switchable gate between two connected wire nodes.

### Heavy Accumulator Banks
- Massive industrial capacitor banks storing up to **4,000,000 FE** per unit.
- Supports blazing **16,384 FE/t** simultaneous charge and discharge rates.
- Automatically pushes energy to connected low-buffer machines and wire connectors.

---

## 6. Heavy Multiblock Power Plant (3x3x3)

The pinnacle of early-to-late industrial power generation in Power Grid is the **Heavy Power Plant**.

### Structural Requirements & Bill of Materials
A complete 3x3x3 Power Plant requires exactly 27 blocks:
- **1x Power Plant Controller** (`powergrid:power_plant_controller`) — Front center block.
- **1x Heavy Alternator Coil** (`powergrid:alternator_coil`) OR **Turbine Rotor** (`powergrid:turbine_rotor`) — Very center block (X=0, Y=0, Z=0 relative to core).
- **1x Industrial Exhaust Stack** (`powergrid:exhaust_stack`) — Top center block.
- **24x Generator Casing** (`powergrid:generator_casing`) — Comprising the entire bottom foundation layer and exterior structural walls.

### Layer-by-Layer Construction Diagram

```
=== LAYER 1 (Foundation Base - Y = 0) ===
[ Casing ] [ Casing ] [ Casing ]
[ Casing ] [ Casing ] [ Casing ]
[ Casing ] [ Casing ] [ Casing ]

=== LAYER 2 (Core & Mechanical - Y = 1) ===
[ Casing ] [ Controller ] [ Casing ]   <- Front Face
[ Casing ] [ Rotor/Coil ] [ Casing ]
[ Casing ] [   Casing   ] [ Casing ]

=== LAYER 3 (Roof & Ventilation - Y = 2) ===
[ Casing ] [   Casing   ] [ Casing ]
[ Casing ] [  Exhaust   ] [ Casing ]   <- Chimney Stack
[ Casing ] [   Casing   ] [ Casing ]
```

### Commissioning with the Engineer's Hammer
1. Verify all 27 blocks are placed accurately according to the diagram above.
2. Hold the **Grid Engineer's Hammer** in your main hand.
3. Right-click the front **Power Plant Controller**.
4. If valid, the structure will flash with a satisfying anvil chime, turning into an active multiblock!

### Thermodynamic Generation & Operational Behavior
- Once assembled, the internal combustion core continuously generates **4,096 FE/t**.
- Generates high-density thermal exhaust plumes visible from dozens of blocks away.
- Possesses an internal reserve buffer of **2,000,000 FE**.
- Features auto-export into adjacent HV Wire Connectors, Transformers, or Accumulator Banks.

---

## 7. Comprehensive Recipe Codex

| Item / Block | Pattern Layout | Ingredients | Output |
| :--- | :--- | :--- | :--- |
| **Grid Engineer's Hammer** | `  I` / ` S ` / `S  ` | 1x Iron Ingot (`I`), 2x Stick (`S`) | 1x Engineer's Hammer |
| **Grid Voltmeter** | ` C ` / `RGR` / ` I ` | 1x Copper (`C`), 2x Redstone (`R`), 1x Glass Pane (`G`), 1x Iron Ingot (`I`) | 1x Voltmeter |
| **LV Copper Wire Coil** | ` C ` / `CSC` / ` C ` | 4x Copper Ingot (`C`), 1x Stick (`S`) | 4x LV Wire Coil |
| **MV Electrum Wire Coil**| ` G ` / `CSC` / ` G ` | 2x Gold Ingot (`G`), 2x Copper Ingot (`C`), 1x Stick (`S`) | 4x MV Wire Coil |
| **HV Steel-Core Coil**   | ` I ` / `ISI` / ` I ` | 4x Iron Ingot (`I`), 1x Stick (`S`) | 4x HV Cable Coil |
| **Treated Wood Pole**    | ` W ` / ` W ` / ` W ` | 3x Spruce/Treated Planks (`W`) | 6x Wood Poles |
| **Pole Crossarm**        | `WWW` / ` S ` / `   ` | 3x Spruce/Treated Planks (`W`), 1x Stick (`S`) | 2x Crossarms |

---

## 8. Cross-Mod Interoperability & Capabilities

Power Grid is built directly on NeoForge's official **Transfer API**:
- **Forge Energy Standard**: Any block registered with `Capabilities.Energy.BLOCK` can directly receive or supply power via `WireConnectorBlock`.
- **Refinery Mod Synergy**: Place an HV Wire Connector directly against an *Industrial Refinery* Diesel Generator or Steam Turbine to instantly export refinery power directly to your municipal power grid.
- **Logistics Mod Synergy**: Feed automation machinery and sorting hubs seamlessly from overhead wire drop lines.

---

## 9. Frequently Asked Questions (FAQ) & Troubleshooting

**Q: My wire won't attach between two connectors!**
> **A**: Verify that both terminals are of the exact same tier (e.g. LV Connector to LV Relay). Also check that the distance does not exceed the wire's maximum span (16 blocks for LV, 32 for MV, 64 for HV). Use your **Grid Voltmeter** to check line status.

**Q: Can players take shock damage from exposed wires?**
> **A**: High-voltage lines carry extreme currents! While LV lines are safe to touch, uninsulated HV transmission cables will deliver a nasty electrical shock if players brush against them without protective gear.

**Q: How do I remove a wire line?**
> **A**: Right-click the terminal with **Insulated Wire Cutters**. All connected coils will cleanly pop off and drop into your inventory.

**Q: Why won't my Power Plant assemble?**
> **A**: Double-check that your center mechanical core is either an `AlternatorCoilBlock` or `TurbineRotorBlock`, the top middle block is an `ExhaustStackBlock`, and the remaining 24 blocks are `GeneratorCasingBlock`. Right-click the controller with the **Grid Engineer's Hammer**.

---

## 📜 License
Power Grid is proudly open-source under the [MIT License](LICENSE).

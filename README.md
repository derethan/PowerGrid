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
4. [Indoor AWG Power Cables](#4-indoor-awg-power-cables)
   - [American Wire Gauge (AWG) Sizing](#american-wire-gauge-awg-sizing)
   - [Minimal Buffering & Network Flow](#minimal-buffering--network-flow)
   - [Tool Interactions & Side Toggling](#tool-interactions--side-toggling)
5. [Overhead Poles & Infrastructure](#5-overhead-poles--infrastructure)
   - [Treated Wood Utility Poles](#treated-wood-utility-poles)
   - [Steel Truss Lattice Towers](#steel-truss-lattice-towers)
   - [Pole Crossarms & Insulator Mounts](#pole-crossarms--insulator-mounts)
6. [Connectors, Relays, & Substations](#6-connectors-relays--substations)
   - [Wire Connectors (Block Interface)](#wire-connectors-block-interface)
   - [Wire Relays (Pass-Through Routing)](#wire-relays-pass-through-routing)
   - [Substation Transformers (Step-Up / Step-Down)](#substation-transformers-step-up--step-down)
   - [High-Voltage Breaker Switches](#high-voltage-breaker-switches)
   - [Heavy Accumulator Banks](#heavy-accumulator-banks)
7. [Comprehensive Recipe Codex](#7-comprehensive-recipe-codex)
8. [Cross-Mod Interoperability & Capabilities](#8-cross-mod-interoperability--capabilities)
9. [Frequently Asked Questions (FAQ) & Troubleshooting](#9-frequently-asked-questions-faq--troubleshooting)

---

## 1. Overview & Philosophy

In modern modded Minecraft, electricity has often been reduced to magic single-block cables hidden beneath flooring. **Power Grid** brings back the romance, realism, and spatial puzzle of genuine electrical infrastructure, deeply inspired by the golden standard set by *Immersive Engineering*.

- **Physical Overhead Lines**: Run catenary copper, electrum, and steel-core wires strung between utility poles, rooftops, and substation yards.
- **Voltage Tier Progression**: Power cannot simply jump arbitrary distances without consequences. Low voltages suffer high transmission loss, demanding step-up transformation to High Voltage (HV) for long-distance cross-country spans.
- **Industrial Transmission Focus**: Build authentic distribution networks with wooden utility poles, steel towers, crossarms, ceramic insulators, substation transformers, and high-voltage breaker switches.
- **Native NeoForge Transfer API**: Built from the ground up for **Minecraft 26.3** and **NeoForge 26.3.0.48-beta**, natively exposing standard `Capabilities.Energy.BLOCK` for zero-lag, instant compatibility with mods like *Industrial Refinery*, *Logistics*, and all Forge Energy (FE) machines.

---

## 2. Essential Engineering Tools

Before building an electrical network, an engineer must craft their diagnostic and installation kit.

### Grid Engineer's Hammer
The quintessential tool of the trade.
- **Block Orientation**: Right-click on directional grid components (transformers, crossarms, breakers) to cycle their horizontal facing clockwise.
- **Connector Mode**: Right-click a Wire Connector to cycle its input/output mode.
- **Cable Side Isolation**: Right-click any face or arm of an Indoor Power Cable to toggle that side connected/disconnected, allowing clean isolation and preventing unintended connections.
- **Durability**: Indestructible hand tool.

### Grid Voltmeter
A precision electronic multimeter for live grid diagnostics.
- Right-click any **Wire Connector** or **Wire Relay** to display:
  - Voltage rating (LV, MV, or HV)
  - Number of connected lines
  - Destination coordinates and span distance of each connected wire
- Right-click any **Indoor Power Cable** to display:
  - Wire gauge rating (18, 16, 14, or 12 AWG)
  - Transferred energy throughput this tick and maximum rated throughput
  - Internal energy buffer status
- Right-click any **Battery / Accumulator / Machine** to inspect:
  - Stored Forge Energy (FE) and maximum buffer capacity

### Insulated Wire Cutters
High-leverage ceramic-insulated wire snips.
- Right-click any **Wire Connector** or **Wire Relay** to immediately sever all attached wire lines, dropping the consumed wire coils back into your hands or onto the ground.
- Right-click any **Indoor Power Cable** to instantly snip and collect the cable piece into your inventory with zero delay.

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
| **HV (High Voltage)** | Steel-Core Aluminum | **16,384 FE/t** | **64 blocks** | 0.5% per block | Cross-country overland grids, heavy industrial transmission |

### Line Resistance & Transmission Loss
Long overhead spans naturally encounter physical electrical resistance. When power travels between two terminals, the net energy delivered to the receiving node equals:

$$\text{Net Received} = \text{Transmitted FE} \times \left(1 - (\text{Distance} \times \text{Loss Rate})\right)$$

*Example*: Sending 1,000 FE over 20 blocks of MV wire (1% loss per block = 20% loss) delivers 800 FE. Stepping up to HV wire (0.5% loss per block = 10% loss) preserves 900 FE!

---

## 4. Indoor AWG Power Cables

While overhead utility wires carry high voltages across long outdoor distances, **Indoor AWG Power Cables** provide clean, direct machine-to-machine power distribution inside workshops, substations, and industrial manufacturing plants.

### American Wire Gauge (AWG) Sizing
Following standard electrical engineering conventions, lower AWG numbers denote thicker conductors and higher energy transfer capacities:

| Cable Gauge | Core Metal Strand | Max Throughput | Internal Buffer | Recommended Use Case |
| :--- | :--- | :--- | :--- | :--- |
| **18 AWG** | Annealed Copper Strand | **128 FE/t** | **128 FE** | Low-power automation, lighting, sorting hubs |
| **16 AWG** | Refined Iron Strand | **512 FE/t** | **512 FE** | Standard workshop machinery, intermediate processing |
| **14 AWG** | High-Tensile Steel Strand | **2,048 FE/t** | **2,048 FE** | Heavy industrial factories, high-speed smelters |
| **12 AWG** | Electrum / Gold Strand | **8,192 FE/t** | **8,192 FE** | Main indoor feeder buses, substation drop interconnects |

### Minimal Buffering & Network Flow
- **1-Tick Buffer Capacity**: Each cable piece buffers exactly one tick worth of rated energy ($1 \times \text{transfer rate}$). Cables act as true conductors rather than giant energy storage batteries, maintaining lightweight memory footprints and preventing energy hoarding.
- **Dynamic Instant Flow Routing**: Energy inserted into any cable is routed along connected cable segments via breadth-first search (`CableNetworkRouter`) and pushed directly into connected machines during the same tick.
- **Bottleneck Preservation**: If you run a high-gauge (thin) cable between lower-gauge (thick) cables, the throughput across that path is naturally throttled to the narrowest cable's rated capacity, preserving realistic electrical physics without artificial loss.
- **Waterloggable**: Fully waterloggable blocks for safe routing through aquatic facilities or industrial basements.

### Tool Interactions & Side Toggling
- **Side Isolation**: Right-click any face or arm of a cable with the **Grid Engineer's Hammer** to disconnect or reconnect that specific face. Disconnected sides render as open air gaps and will not connect to adjacent machines or cables.
- **Fast Dismantling**: Right-click any cable with **Insulated Wire Cutters** to instantly dismantle it directly into your inventory.
- **Diagnostics**: Right-click any cable with the **Grid Voltmeter** to inspect its current throughput, gauge rating, and internal buffer.

---

## 5. Overhead Poles & Infrastructure

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

## 6. Connectors, Relays, & Substations

### Visual Quality & 3D Element Models
- **32x32 High-Fidelity Default**: All blocks and items feature handcrafted 32x32 pixel art textures with creosote grain, metallic specular highlights, and glazed ceramic sheens.
- **1024x1024 Ultra-HD Resource Pack**: Includes a dedicated `(v1.0) 1024x Power Grid.zip` resource pack featuring photorealistic ultra-high-definition textures for all grid equipment.
- **Full 3D Element Geometry & Precision Alignment**:
  - **Crossarm-End Alignment**: When mounted on the end face of a horizontal crossarm, connectors and relays automatically center along the crossarm's beam axis ($Y=13.0$), eliminating any vertical sagging or drooping below the wood beam.
  - **Pole & Crossarm Standoff Brackets**: When placed on the side of a round wooden pole or side of a crossarm, heavy structural steel standoff channel brackets extend 5px backwards across the block boundary to clasp the timber flush, eliminating floating gaps.
  - **Flush Surface Mounting**: Mounted flush on full blocks, ceilings, and directly on top of poles and crossarm beams.

### Wire Connectors (Block Interface)
- Mounted on the face of any machine, generator, or battery.
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
- Equipped with a 64,000 FE high-speed balancing buffer supporting up to **16,384 FE/t** throughput.

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

## 7. Comprehensive Recipe Codex

| Item / Block | Pattern Layout | Ingredients | Output |
| :--- | :--- | :--- | :--- |
| **Grid Engineer's Hammer** | `  I` / ` S ` / `S  ` | 1x Iron Ingot (`I`), 2x Stick (`S`) | 1x Engineer's Hammer |
| **Grid Voltmeter** | ` C ` / `RGR` / ` I ` | 1x Copper (`C`), 2x Redstone (`R`), 1x Glass Pane (`G`), 1x Iron Ingot (`I`) | 1x Voltmeter |
| **Insulated Wire Cutters** | `I I` / ` S ` / `S S` | 2x Iron Ingot (`I`), 3x Stick (`S`) | 1x Wire Cutters |
| **Copper Wire Strand** | `CCC` (Shapeless) | 3x Copper Ingot (`C`) | 4x Copper Wire Strand |
| **Iron Wire Strand** | `III` (Shapeless) | 3x Iron Ingot (`I`) | 4x Iron Wire Strand |
| **Steel Wire Strand** | Blasting / Smelting | 1x Iron Wire Strand (or 3x Steel Ingot) | 1x (or 4x) Steel Wire Strand |
| **Gold Wire Strand** | `GGG` (Shapeless) | 3x Gold Ingot (`G`) | 4x Gold Wire Strand |
| **18 AWG Power Cable** | ` K ` / ` W ` / ` K ` | 1x Copper Wire Strand (`W`), 2x Rubber / Dried Kelp (`K`) | 6x 18 AWG Cable |
| **16 AWG Power Cable** | ` K ` / ` W ` / ` K ` | 1x Iron Wire Strand (`W`), 2x Rubber / Dried Kelp (`K`) | 6x 16 AWG Cable |
| **14 AWG Power Cable** | ` K ` / ` W ` / ` K ` | 1x Steel Wire Strand (`W`), 2x Rubber / Dried Kelp (`K`) | 6x 14 AWG Cable |
| **12 AWG Power Cable** | ` K ` / ` W ` / ` K ` | 1x Gold Wire Strand (`W`), 2x Rubber / Dried Kelp (`K`) | 6x 12 AWG Cable |
| **LV Copper Wire Coil** | ` C ` / `CSC` / ` C ` | 4x Copper Ingot (`C`), 1x Stick (`S`) | 4x LV Wire Coil |
| **MV Electrum Wire Coil**| ` G ` / `CSC` / ` G ` | 2x Gold Ingot (`G`), 2x Copper Ingot (`C`), 1x Stick (`S`) | 4x MV Wire Coil |
| **HV Steel-Core Coil**   | ` I ` / `ISI` / ` I ` | 4x Iron Ingot (`I`), 1x Stick (`S`) | 4x HV Cable Coil |
| **Treated Wood Pole**    | ` W ` / ` W ` / ` W ` | 3x Spruce/Treated Planks (`W`) | 6x Wood Poles |
| **Pole Crossarm**        | `WWW` / ` S ` / `   ` | 3x Spruce/Treated Planks (`W`), 1x Stick (`S`) | 2x Crossarms |
| **Safety Breaker Switch**| ` R ` / ` L ` / ` I ` | 1x Redstone (`R`), 1x Lever (`L`), 1x Iron Ingot (`I`) | 1x Breaker Switch |

---

## 8. Cross-Mod Interoperability & Capabilities

Power Grid is built directly on NeoForge's official **Transfer API**:
- **Forge Energy Standard**: Any block registered with `Capabilities.Energy.BLOCK` can directly receive or supply power via `WireConnectorBlock` or **Indoor AWG Power Cables**.
- **Refinery Mod Synergy**: Place an HV Wire Connector directly against an *Industrial Refinery* Diesel Generator or Steam Turbine to instantly export refinery power directly to your municipal power grid.
- **Logistics Mod Synergy**: Feed automation machinery, item pipes, and craft tables directly from indoor AWG cables or overhead drop lines.

---

## 9. Frequently Asked Questions (FAQ) & Troubleshooting

**Q: My wire won't attach between two connectors!**
> **A**: Verify that both terminals are of the exact same tier (e.g. LV Connector to LV Relay). Also check that the distance does not exceed the wire's maximum span (16 blocks for LV, 32 for MV, 64 for HV). Use your **Grid Voltmeter** to check line status.

**Q: Can players take shock damage from exposed wires?**
> **A**: High-voltage lines carry extreme currents! While LV lines are safe to touch, uninsulated HV transmission cables will deliver a nasty electrical shock if players brush against them without protective gear.

**Q: How do I remove a wire line?**
> **A**: Right-click the terminal with **Insulated Wire Cutters**. All connected coils will cleanly pop off and drop into your inventory.

---

## 📜 License
Power Grid is proudly open-source under the [MIT License](LICENSE).

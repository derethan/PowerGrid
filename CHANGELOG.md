# Changelog — Power Grid

All notable changes to the **Power Grid** mod are documented in this file.

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

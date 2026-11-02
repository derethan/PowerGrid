package com.powergrid;

import com.mojang.logging.LogUtils;
import com.powergrid.block.*;
import com.powergrid.block.entity.*;
import com.powergrid.grid.CableGauge;
import com.powergrid.grid.WireType;
import com.powergrid.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(PowerGridMod.MODID)
public class PowerGridMod {
    public static final String MODID = "powergrid";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Registries
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // === Blocks & Simple Block Items ===

    // Poles & Structure
    public static final DeferredBlock<TreatedWoodPoleBlock> TREATED_WOOD_POLE = BLOCKS.registerBlock("treated_wood_pole",
            TreatedWoodPoleBlock::new);
    public static final DeferredItem<BlockItem> TREATED_WOOD_POLE_ITEM = ITEMS.registerSimpleBlockItem("treated_wood_pole", TREATED_WOOD_POLE);

    public static final DeferredBlock<SteelTrussPoleBlock> STEEL_TRUSS_POLE = BLOCKS.registerBlock("steel_truss_pole",
            SteelTrussPoleBlock::new);
    public static final DeferredItem<BlockItem> STEEL_TRUSS_POLE_ITEM = ITEMS.registerSimpleBlockItem("steel_truss_pole", STEEL_TRUSS_POLE);

    public static final DeferredBlock<PoleCrossarmBlock> POLE_CROSSARM = BLOCKS.registerBlock("pole_crossarm",
            PoleCrossarmBlock::new);
    public static final DeferredItem<BlockItem> POLE_CROSSARM_ITEM = ITEMS.registerSimpleBlockItem("pole_crossarm", POLE_CROSSARM);

    // Wire Connectors
    public static final DeferredBlock<WireConnectorBlock> LV_WIRE_CONNECTOR = BLOCKS.registerBlock("lv_wire_connector",
            p -> new WireConnectorBlock(WireType.LV, p));
    public static final DeferredItem<BlockItem> LV_WIRE_CONNECTOR_ITEM = ITEMS.registerSimpleBlockItem("lv_wire_connector", LV_WIRE_CONNECTOR);

    public static final DeferredBlock<WireConnectorBlock> MV_WIRE_CONNECTOR = BLOCKS.registerBlock("mv_wire_connector",
            p -> new WireConnectorBlock(WireType.MV, p));
    public static final DeferredItem<BlockItem> MV_WIRE_CONNECTOR_ITEM = ITEMS.registerSimpleBlockItem("mv_wire_connector", MV_WIRE_CONNECTOR);

    public static final DeferredBlock<WireConnectorBlock> HV_WIRE_CONNECTOR = BLOCKS.registerBlock("hv_wire_connector",
            p -> new WireConnectorBlock(WireType.HV, p));
    public static final DeferredItem<BlockItem> HV_WIRE_CONNECTOR_ITEM = ITEMS.registerSimpleBlockItem("hv_wire_connector", HV_WIRE_CONNECTOR);

    // Wire Relays
    public static final DeferredBlock<WireRelayBlock> LV_WIRE_RELAY = BLOCKS.registerBlock("lv_wire_relay",
            p -> new WireRelayBlock(WireType.LV, p));
    public static final DeferredItem<BlockItem> LV_WIRE_RELAY_ITEM = ITEMS.registerSimpleBlockItem("lv_wire_relay", LV_WIRE_RELAY);

    public static final DeferredBlock<WireRelayBlock> MV_WIRE_RELAY = BLOCKS.registerBlock("mv_wire_relay",
            p -> new WireRelayBlock(WireType.MV, p));
    public static final DeferredItem<BlockItem> MV_WIRE_RELAY_ITEM = ITEMS.registerSimpleBlockItem("mv_wire_relay", MV_WIRE_RELAY);

    public static final DeferredBlock<WireRelayBlock> HV_WIRE_RELAY = BLOCKS.registerBlock("hv_wire_relay",
            p -> new WireRelayBlock(WireType.HV, p));
    public static final DeferredItem<BlockItem> HV_WIRE_RELAY_ITEM = ITEMS.registerSimpleBlockItem("hv_wire_relay", HV_WIRE_RELAY);

    // Indoor AWG Power Cables
    public static final DeferredBlock<PowerCableBlock> CABLE_18_GAUGE = BLOCKS.registerBlock("cable_18_gauge",
            p -> new PowerCableBlock(CableGauge.GAUGE_18, p));
    public static final DeferredItem<PowerCableBlockItem> CABLE_18_GAUGE_ITEM = ITEMS.registerItem("cable_18_gauge",
            p -> new PowerCableBlockItem(CABLE_18_GAUGE.get(), p));

    public static final DeferredBlock<PowerCableBlock> CABLE_16_GAUGE = BLOCKS.registerBlock("cable_16_gauge",
            p -> new PowerCableBlock(CableGauge.GAUGE_16, p));
    public static final DeferredItem<PowerCableBlockItem> CABLE_16_GAUGE_ITEM = ITEMS.registerItem("cable_16_gauge",
            p -> new PowerCableBlockItem(CABLE_16_GAUGE.get(), p));

    public static final DeferredBlock<PowerCableBlock> CABLE_14_GAUGE = BLOCKS.registerBlock("cable_14_gauge",
            p -> new PowerCableBlock(CableGauge.GAUGE_14, p));
    public static final DeferredItem<PowerCableBlockItem> CABLE_14_GAUGE_ITEM = ITEMS.registerItem("cable_14_gauge",
            p -> new PowerCableBlockItem(CABLE_14_GAUGE.get(), p));

    public static final DeferredBlock<PowerCableBlock> CABLE_12_GAUGE = BLOCKS.registerBlock("cable_12_gauge",
            p -> new PowerCableBlock(CableGauge.GAUGE_12, p));
    public static final DeferredItem<PowerCableBlockItem> CABLE_12_GAUGE_ITEM = ITEMS.registerItem("cable_12_gauge",
            p -> new PowerCableBlockItem(CABLE_12_GAUGE.get(), p));

    // Transformer & Switchgear
    public static final DeferredBlock<TransformerBlock> TRANSFORMER = BLOCKS.registerBlock("transformer",
            TransformerBlock::new);
    public static final DeferredItem<BlockItem> TRANSFORMER_ITEM = ITEMS.registerSimpleBlockItem("transformer", TRANSFORMER);

    public static final DeferredBlock<BreakerSwitchBlock> BREAKER_SWITCH = BLOCKS.registerBlock("breaker_switch",
            BreakerSwitchBlock::new);
    public static final DeferredItem<BlockItem> BREAKER_SWITCH_ITEM = ITEMS.registerSimpleBlockItem("breaker_switch", BREAKER_SWITCH);

    // Storage
    public static final DeferredBlock<HeavyAccumulatorBlock> HEAVY_ACCUMULATOR = BLOCKS.registerBlock("heavy_accumulator",
            HeavyAccumulatorBlock::new);
    public static final DeferredItem<BlockItem> HEAVY_ACCUMULATOR_ITEM = ITEMS.registerSimpleBlockItem("heavy_accumulator", HEAVY_ACCUMULATOR);

    // === Tools & Usable Items ===
    public static final DeferredItem<Item> GRID_ICON = ITEMS.registerItem("grid_icon",
            Item::new);
    public static final DeferredItem<EngineerHammerItem> ENGINEER_HAMMER = ITEMS.registerItem("engineer_hammer",
            EngineerHammerItem::new);
    public static final DeferredItem<VoltmeterItem> VOLTMETER = ITEMS.registerItem("voltmeter",
            VoltmeterItem::new);
    public static final DeferredItem<WireCuttersItem> WIRE_CUTTERS = ITEMS.registerItem("wire_cutters",
            WireCuttersItem::new);

    public static final DeferredItem<WireCoilItem> LV_WIRE_COIL = ITEMS.registerItem("lv_wire_coil",
            p -> new WireCoilItem(WireType.LV, p));
    public static final DeferredItem<WireCoilItem> MV_WIRE_COIL = ITEMS.registerItem("mv_wire_coil",
            p -> new WireCoilItem(WireType.MV, p));
    public static final DeferredItem<WireCoilItem> HV_WIRE_COIL = ITEMS.registerItem("hv_wire_coil",
            p -> new WireCoilItem(WireType.HV, p));

    // Crafting Materials
    public static final DeferredItem<MaterialItem> TREATED_WOOD = ITEMS.registerItem("treated_wood",
            p -> new MaterialItem(p, "tooltip.powergrid.treated_wood.desc"));
    public static final DeferredItem<MaterialItem> COPPER_WIRE = ITEMS.registerItem("copper_wire",
            p -> new MaterialItem(p, "tooltip.powergrid.copper_wire.desc"));
    public static final DeferredItem<MaterialItem> ELECTRUM_WIRE = ITEMS.registerItem("electrum_wire",
            p -> new MaterialItem(p, "tooltip.powergrid.electrum_wire.desc"));
    public static final DeferredItem<MaterialItem> STEEL_STRAND = ITEMS.registerItem("steel_strand",
            p -> new MaterialItem(p, "tooltip.powergrid.steel_strand.desc"));
    public static final DeferredItem<MaterialItem> CERAMIC_INSULATOR = ITEMS.registerItem("ceramic_insulator",
            p -> new MaterialItem(p, "tooltip.powergrid.ceramic_insulator.desc"));

    // === Block Entity Types ===
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WireConnectorBlockEntity>> WIRE_CONNECTOR_BE =
            BLOCK_ENTITY_TYPES.register("wire_connector",
                    () -> new BlockEntityType<>(WireConnectorBlockEntity::new,
                            LV_WIRE_CONNECTOR.get(), MV_WIRE_CONNECTOR.get(), HV_WIRE_CONNECTOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WireRelayBlockEntity>> WIRE_RELAY_BE =
            BLOCK_ENTITY_TYPES.register("wire_relay",
                    () -> new BlockEntityType<>(WireRelayBlockEntity::new,
                            LV_WIRE_RELAY.get(), MV_WIRE_RELAY.get(), HV_WIRE_RELAY.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PowerCableBlockEntity>> POWER_CABLE_BE =
            BLOCK_ENTITY_TYPES.register("power_cable",
                    () -> new BlockEntityType<>(PowerCableBlockEntity::new,
                            CABLE_18_GAUGE.get(), CABLE_16_GAUGE.get(), CABLE_14_GAUGE.get(), CABLE_12_GAUGE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TransformerBlockEntity>> TRANSFORMER_BE =
            BLOCK_ENTITY_TYPES.register("transformer",
                    () -> new BlockEntityType<>(TransformerBlockEntity::new, TRANSFORMER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BreakerSwitchBlockEntity>> BREAKER_SWITCH_BE =
            BLOCK_ENTITY_TYPES.register("breaker_switch",
                    () -> new BlockEntityType<>(BreakerSwitchBlockEntity::new, BREAKER_SWITCH.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeavyAccumulatorBlockEntity>> HEAVY_ACCUMULATOR_BE =
            BLOCK_ENTITY_TYPES.register("heavy_accumulator",
                    () -> new BlockEntityType<>(HeavyAccumulatorBlockEntity::new, HEAVY_ACCUMULATOR.get()));

    // === Creative Mode Tab ===
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> POWER_GRID_TAB = CREATIVE_MODE_TABS.register("powergrid_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.powergrid"))
                    .icon(() -> GRID_ICON.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        // Badge / Icon
                        output.accept(GRID_ICON.get());
                        // Tools
                        output.accept(ENGINEER_HAMMER.get());
                        output.accept(VOLTMETER.get());
                        output.accept(WIRE_CUTTERS.get());

                        // Wire Coils
                        output.accept(LV_WIRE_COIL.get());
                        output.accept(MV_WIRE_COIL.get());
                        output.accept(HV_WIRE_COIL.get());

                        // Components
                        output.accept(TREATED_WOOD.get());
                        output.accept(COPPER_WIRE.get());
                        output.accept(ELECTRUM_WIRE.get());
                        output.accept(STEEL_STRAND.get());
                        output.accept(CERAMIC_INSULATOR.get());

                        // Poles & Structure
                        output.accept(TREATED_WOOD_POLE_ITEM.get());
                        output.accept(STEEL_TRUSS_POLE_ITEM.get());
                        output.accept(POLE_CROSSARM_ITEM.get());

                        // Connectors & Relays
                        output.accept(LV_WIRE_CONNECTOR_ITEM.get());
                        output.accept(MV_WIRE_CONNECTOR_ITEM.get());
                        output.accept(HV_WIRE_CONNECTOR_ITEM.get());
                        output.accept(LV_WIRE_RELAY_ITEM.get());
                        output.accept(MV_WIRE_RELAY_ITEM.get());
                        output.accept(HV_WIRE_RELAY_ITEM.get());

                        // Indoor Power Cables
                        output.accept(CABLE_18_GAUGE_ITEM.get());
                        output.accept(CABLE_16_GAUGE_ITEM.get());
                        output.accept(CABLE_14_GAUGE_ITEM.get());
                        output.accept(CABLE_12_GAUGE_ITEM.get());

                        // Grid Gear
                        output.accept(TRANSFORMER_ITEM.get());
                        output.accept(BREAKER_SWITCH_ITEM.get());
                        output.accept(HEAVY_ACCUMULATOR_ITEM.get());
                    })
                    .build());

    public PowerGridMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register configuration specification
        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.LOCAL, com.powergrid.config.PowerGridConfig.SPEC, "powergrid.toml");

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);

        LOGGER.info("Power Grid Mod initialized for Minecraft 26.3 NeoForge 26.3.0.48!");
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Wire Connectors
        event.registerBlockEntity(Capabilities.Energy.BLOCK, WIRE_CONNECTOR_BE.get(),
                (be, side) -> be.energyStorage);

        // Power Cables
        event.registerBlockEntity(Capabilities.Energy.BLOCK, POWER_CABLE_BE.get(),
                PowerCableBlockEntity::getSideEnergyHandler);

        // Transformer
        event.registerBlockEntity(Capabilities.Energy.BLOCK, TRANSFORMER_BE.get(),
                (be, side) -> be.energyStorage);

        // Heavy Accumulator (Capacitor Bank)
        event.registerBlockEntity(Capabilities.Energy.BLOCK, HEAVY_ACCUMULATOR_BE.get(),
                (be, side) -> be.energyStorage);
    }
}

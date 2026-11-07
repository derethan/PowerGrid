package com.powergrid.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class PowerGridConfig {
    public static final ModConfigSpec SPEC;
    public static final PowerGridConfig CONFIG;

    static {
        Pair<PowerGridConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(PowerGridConfig::new);
        SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    public final ModConfigSpec.DoubleValue lvLossPerBlock;
    public final ModConfigSpec.DoubleValue mvLossPerBlock;
    public final ModConfigSpec.DoubleValue hvLossPerBlock;

    public final ModConfigSpec.IntValue lvLossFreeDistance;
    public final ModConfigSpec.IntValue mvLossFreeDistance;
    public final ModConfigSpec.IntValue hvLossFreeDistance;

    public final ModConfigSpec.IntValue waterWheelFePerDrop;
    public final ModConfigSpec.IntValue waterWheelMaxDrop;
    public final ModConfigSpec.IntValue waterWheelCapacity;
    public final ModConfigSpec.IntValue waterWheelMaxTransfer;

    public final ModConfigSpec.IntValue solarPanelBaseFePerTick;
    public final ModConfigSpec.IntValue solarPanelGoldFePerTick;
    public final ModConfigSpec.IntValue solarPanelDiamondFePerTick;

    public final ModConfigSpec.IntValue solarPanelBaseCapacity;
    public final ModConfigSpec.IntValue solarPanelGoldCapacity;
    public final ModConfigSpec.IntValue solarPanelDiamondCapacity;

    public final ModConfigSpec.IntValue solarPanelBaseMaxExport;
    public final ModConfigSpec.IntValue solarPanelGoldMaxExport;
    public final ModConfigSpec.IntValue solarPanelDiamondMaxExport;

    public final ModConfigSpec.IntValue windmillMinGroundClearance;
    public final ModConfigSpec.IntValue windmillBaseFePerTick;
    public final ModConfigSpec.IntValue windmillFePerBlockAboveMin;
    public final ModConfigSpec.IntValue windmillMaxFePerTick;
    public final ModConfigSpec.IntValue windmillMaxClearanceChecked;
    public final ModConfigSpec.IntValue windmillCapacity;
    public final ModConfigSpec.IntValue windmillMaxTransfer;

    public PowerGridConfig(ModConfigSpec.Builder builder) {
        builder.push("Transmission_Line_Loss");

        lvLossPerBlock = builder
                .comment("Energy transmission loss fraction per block for Low Voltage (LV) copper wires.",
                        "Intended production balance value: 0.02 (2.0% loss per block).",
                        "Default is set to 0.0 for initial testing and grid stability.")
                .defineInRange("lvLossPerBlock", 0.0, 0.0, 1.0);

        mvLossPerBlock = builder
                .comment("Energy transmission loss fraction per block for Medium Voltage (MV) electrum wires.",
                        "Intended production balance value: 0.01 (1.0% loss per block).",
                        "Default is set to 0.0 for initial testing and grid stability.")
                .defineInRange("mvLossPerBlock", 0.0, 0.0, 1.0);

        hvLossPerBlock = builder
                .comment("Energy transmission loss fraction per block for High Voltage (HV) steel-core wires.",
                        "Intended production balance value: 0.005 (0.5% loss per block).",
                        "Default is set to 0.0 for initial testing and grid stability.")
                .defineInRange("hvLossPerBlock", 0.0, 0.0, 1.0);

        builder.pop();

        builder.push("Loss_Free_Distance_Thresholds");

        lvLossFreeDistance = builder
                .comment("Maximum distance in blocks for Low Voltage (LV) wire runs before any line loss begins to apply.",
                        "Wire runs under or equal to this distance incur 0% transmission loss.",
                        "Default value: 8 blocks.")
                .defineInRange("lvLossFreeDistance", 8, 0, 128);

        mvLossFreeDistance = builder
                .comment("Maximum distance in blocks for Medium Voltage (MV) wire runs before any line loss begins to apply.",
                        "Wire runs under or equal to this distance incur 0% transmission loss.",
                        "Default value: 16 blocks.")
                .defineInRange("mvLossFreeDistance", 16, 0, 256);

        hvLossFreeDistance = builder
                .comment("Maximum distance in blocks for High Voltage (HV) wire runs before any line loss begins to apply.",
                        "Wire runs under or equal to this distance incur 0% transmission loss.",
                        "Default value: 24 blocks.")
                .defineInRange("hvLossFreeDistance", 24, 0, 512);

        builder.pop();

        builder.push("Wooden_Water_Wheel");

        waterWheelFePerDrop = builder
                .comment("Base FE/tick generated per vertical height level of flowing water drop.",
                        "Default: 10 FE/tick per drop level.")
                .defineInRange("waterWheelFePerDrop", 10, 1, 1000);

        waterWheelMaxDrop = builder
                .comment("Maximum water drop height levels counted for energy generation.",
                        "Default: 5 height levels.")
                .defineInRange("waterWheelMaxDrop", 5, 1, 32);

        waterWheelCapacity = builder
                .comment("Internal energy buffer capacity of the water wheel center block in FE.",
                        "Default: 10000 FE.")
                .defineInRange("waterWheelCapacity", 10_000, 100, 1_000_000);

        waterWheelMaxTransfer = builder
                .comment("Maximum rate in FE/t that the water wheel pushes energy along the axle stack or into external consumers.",
                        "Default: 2000 FE/t.")
                .defineInRange("waterWheelMaxTransfer", 2000, 10, 100_000);

        builder.pop();

        builder.push("Solar_Panels");

        solarPanelBaseFePerTick = builder
                .comment("Energy generation rate of standard Solar Panels in FE/tick during full daylight.",
                        "Default: 1 FE/t.")
                .defineInRange("solarPanelBaseFePerTick", 1, 1, 1000);

        solarPanelGoldFePerTick = builder
                .comment("Energy generation rate of Gold Solar Panels in FE/tick during full daylight (2x baseline).",
                        "Default: 2 FE/t.")
                .defineInRange("solarPanelGoldFePerTick", 2, 1, 2000);

        solarPanelDiamondFePerTick = builder
                .comment("Energy generation rate of Diamond Solar Panels in FE/tick during full daylight (5x baseline).",
                        "Default: 5 FE/t.")
                .defineInRange("solarPanelDiamondFePerTick", 5, 1, 5000);

        solarPanelBaseCapacity = builder
                .comment("Internal energy storage capacity of standard Solar Panels in FE.",
                        "Default: 10,000 FE.")
                .defineInRange("solarPanelBaseCapacity", 10_000, 100, 1_000_000);

        solarPanelGoldCapacity = builder
                .comment("Internal energy storage capacity of Gold Solar Panels in FE.",
                        "Default: 20,000 FE.")
                .defineInRange("solarPanelGoldCapacity", 20_000, 200, 2_000_000);

        solarPanelDiamondCapacity = builder
                .comment("Internal energy storage capacity of Diamond Solar Panels in FE.",
                        "Default: 50,000 FE.")
                .defineInRange("solarPanelDiamondCapacity", 50_000, 500, 5_000_000);

        solarPanelBaseMaxExport = builder
                .comment("Maximum energy export rate of standard Solar Panels in FE/tick.",
                        "Default: 100 FE/t.")
                .defineInRange("solarPanelBaseMaxExport", 100, 1, 10_000);

        solarPanelGoldMaxExport = builder
                .comment("Maximum energy export rate of Gold Solar Panels in FE/tick.",
                        "Default: 200 FE/t.")
                .defineInRange("solarPanelGoldMaxExport", 200, 1, 20_000);

        solarPanelDiamondMaxExport = builder
                .comment("Maximum energy export rate of Diamond Solar Panels in FE/tick.",
                        "Default: 500 FE/t.")
                .defineInRange("solarPanelDiamondMaxExport", 500, 1, 50_000);

        builder.pop();

        builder.push("Windmill_Rotor");

        windmillMinGroundClearance = builder
                .comment("Minimum air blocks required directly beneath the rotor bottom to operate.",
                        "Default: 3 blocks.")
                .defineInRange("windmillMinGroundClearance", 3, 1, 32);

        windmillBaseFePerTick = builder
                .comment("Base FE/tick generated at minimum ground clearance.",
                        "Default: 10 FE/t.")
                .defineInRange("windmillBaseFePerTick", 10, 1, 1000);

        windmillFePerBlockAboveMin = builder
                .comment("Additional FE/tick generated for each air block above minimum ground clearance.",
                        "Default: 2 FE/t per block.")
                .defineInRange("windmillFePerBlockAboveMin", 2, 0, 100);

        windmillMaxFePerTick = builder
                .comment("Maximum generation ceiling in FE/tick for high-altitude wind rotors.",
                        "Default: 64 FE/t.")
                .defineInRange("windmillMaxFePerTick", 64, 10, 10_000);

        windmillMaxClearanceChecked = builder
                .comment("Maximum vertical depth in blocks checked beneath rotor bottom for ground clearance.",
                        "Default: 32 blocks.")
                .defineInRange("windmillMaxClearanceChecked", 32, 3, 128);

        windmillCapacity = builder
                .comment("Internal energy buffer capacity of the windmill rotor center block in FE.",
                        "Default: 10,000 FE.")
                .defineInRange("windmillCapacity", 10_000, 100, 1_000_000);

        windmillMaxTransfer = builder
                .comment("Maximum rate in FE/t that the windmill rotor pushes energy along the axle stack or to external consumers.",
                        "Default: 2000 FE/t.")
                .defineInRange("windmillMaxTransfer", 2000, 10, 100_000);

        builder.pop();
    }
}

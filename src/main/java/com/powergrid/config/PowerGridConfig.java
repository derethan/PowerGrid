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
    }
}

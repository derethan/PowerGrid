package com.powergrid.block.entity;

import com.powergrid.PowerGridMod;
import com.powergrid.block.WireConnectorBlock;
import com.powergrid.grid.ConnectorMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class HeavyAccumulatorBlockEntity extends BlockEntity {
    public static final int CAPACITY = 4_000_000;
    public static final int MAX_RATE = 16_384;

    public final SimpleEnergyHandler energyStorage = new SimpleEnergyHandler(CAPACITY, MAX_RATE, MAX_RATE);

    public HeavyAccumulatorBlockEntity(BlockPos pos, BlockState state) {
        super(PowerGridMod.HEAVY_ACCUMULATOR_BE.get(), pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        int stored = this.energyStorage.getAmountAsInt();
        if (stored <= 0) return;

        // Auto-output energy into adjacent consumers
        for (Direction dir : Direction.values()) {
            BlockPos targetPos = pos.relative(dir);

            // Avoid pushing back into a Wire Connector unless it is explicitly set to INPUT mode
            BlockState targetState = level.getBlockState(targetPos);
            if (targetState.getBlock() instanceof WireConnectorBlock) {
                BlockEntity be = level.getBlockEntity(targetPos);
                if (be instanceof WireConnectorBlockEntity connector) {
                    if (connector.getMode() != ConnectorMode.INPUT) {
                        continue;
                    }
                }
            }

            EnergyHandler targetEnergy = level.getCapability(Capabilities.Energy.BLOCK, targetPos, dir.getOpposite());
            if (targetEnergy != null) {
                int toPush = Math.min(this.energyStorage.getAmountAsInt(), MAX_RATE);
                try (var tx = Transaction.openRoot()) {
                    int extracted = this.energyStorage.extract(toPush, tx);
                    if (extracted > 0) {
                        int accepted = targetEnergy.insert(extracted, tx);
                        if (accepted == extracted) {
                            tx.commit();
                            setChanged();
                            if (this.energyStorage.getAmountAsInt() <= 0) break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("storedEnergy", this.energyStorage.getAmountAsInt());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int stored = input.getIntOr("storedEnergy", 0);
        this.energyStorage.set(stored);
    }
}

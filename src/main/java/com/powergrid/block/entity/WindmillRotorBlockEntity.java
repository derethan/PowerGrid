package com.powergrid.block.entity;

import com.powergrid.PowerGridMod;
import com.powergrid.block.WindmillRotorBlock;
import com.powergrid.block.WindmillRotorPart;
import com.powergrid.config.PowerGridConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WindmillRotorBlockEntity extends BlockEntity {
    public static final int DEFAULT_CAPACITY = 10_000;
    public static final int DEFAULT_MAX_TRANSFER = 2_000;

    public final SimpleEnergyHandler energyStorage = new SimpleEnergyHandler(DEFAULT_CAPACITY, DEFAULT_MAX_TRANSFER, DEFAULT_MAX_TRANSFER);

    // Dynamic wind state
    private int groundClearance = 0;
    private int feGeneratedLastTick = 0;
    private boolean isSpinning = false;
    private float spinSpeed = 0.0f;

    public WindmillRotorBlockEntity(BlockPos pos, BlockState state) {
        super(PowerGridMod.WINDMILL_ROTOR_BE.get(), pos, state);
    }

    public EnergyHandler getEnergyStorage(@Nullable Direction side) {
        return this.energyStorage;
    }

    public int getGroundClearance() {
        return this.groundClearance;
    }

    public int getFeGeneratedLastTick() {
        return this.feGeneratedLastTick;
    }

    public boolean isSpinning() {
        return this.isSpinning;
    }

    public float getSpinSpeedDegrees() {
        if (!this.isSpinning) return 0.0f;
        int maxFe = PowerGridConfig.CONFIG != null ? PowerGridConfig.CONFIG.windmillMaxFePerTick.get() : 64;
        float progress = Math.min(1.0f, (float) this.feGeneratedLastTick / (float) Math.max(1, maxFe));
        return 2.0f + progress * 4.0f; // 2.0 to 6.0 deg/tick
    }

    public void clientTick() {
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(WindmillRotorBlock.FACING);

        // 1. Calculate ground clearance (air blocks directly below the bottom of the rotor)
        int clearance = calculateGroundClearance(level, pos);
        this.groundClearance = clearance;

        int minClearance = PowerGridConfig.CONFIG.windmillMinGroundClearance.get();
        if (clearance >= minClearance) {
            this.isSpinning = true;

            int baseFe = PowerGridConfig.CONFIG.windmillBaseFePerTick.get();
            int fePerBlock = PowerGridConfig.CONFIG.windmillFePerBlockAboveMin.get();
            int maxFe = PowerGridConfig.CONFIG.windmillMaxFePerTick.get();

            int extra = clearance - minClearance;
            int gen = Math.min(maxFe, baseFe + extra * fePerBlock);

            // Weather bonuses for wind turbines
            if (level.isThundering()) {
                gen = (int) (gen * 1.5f);
            } else if (level.isRaining()) {
                gen = (int) (gen * 1.2f);
            }

            this.feGeneratedLastTick = gen;

            try (var tx = Transaction.openRoot()) {
                this.energyStorage.insert(this.feGeneratedLastTick, tx);
                tx.commit();
            }

            if (!state.getValue(WindmillRotorBlock.OPERATING)) {
                level.setBlock(pos, state.setValue(WindmillRotorBlock.OPERATING, true), 3);
            }
        } else {
            this.isSpinning = false;
            this.feGeneratedLastTick = 0;

            if (state.getValue(WindmillRotorBlock.OPERATING)) {
                level.setBlock(pos, state.setValue(WindmillRotorBlock.OPERATING, false), 3);
            }
        }

        // 2. Axle Stack Coordinated Energy Distribution
        Direction axleNeg = (facing.getAxis() == Direction.Axis.Z) ? Direction.NORTH : Direction.WEST;
        Direction axlePos = (facing.getAxis() == Direction.Axis.Z) ? Direction.SOUTH : Direction.EAST;

        if (!isStackedNeighbor(level, pos, facing, axleNeg)) {
            processAxleStack(level, pos, facing, axleNeg, axlePos);
        }

        // Periodic sync to client
        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(pos, state, state, 2);
        }
    }

    public static int calculateGroundClearance(Level level, BlockPos centerPos) {
        BlockPos bottomPos = centerPos.below();
        int maxCheck = PowerGridConfig.CONFIG != null ? PowerGridConfig.CONFIG.windmillMaxClearanceChecked.get() : 32;
        int clearance = 0;

        for (int i = 1; i <= maxCheck; i++) {
            BlockPos checkPos = bottomPos.below(i);
            if (checkPos.getY() < level.getMinY()) break;

            BlockState checkState = level.getBlockState(checkPos);
            if (checkState.isAir()) {
                clearance++;
            } else {
                break; // Hit ground or obstacle
            }
        }

        return clearance;
    }

    private static boolean isStackedNeighbor(Level level, BlockPos pos, Direction facing, Direction dir) {
        BlockPos neighborPos = pos.relative(dir);
        if (!level.isLoaded(neighborPos)) return false;
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof WindmillRotorBlock) {
            return neighborState.getValue(WindmillRotorBlock.FACING).getAxis() == facing.getAxis()
                    && neighborState.getValue(WindmillRotorBlock.PART) == WindmillRotorPart.CENTER;
        }
        return false;
    }

    private void processAxleStack(Level level, BlockPos startPos, Direction facing, Direction axleNeg, Direction axlePos) {
        List<WindmillRotorBlockEntity> stack = new ArrayList<>();
        BlockPos cur = startPos;
        while (cur != null && level.isLoaded(cur)) {
            BlockEntity be = level.getBlockEntity(cur);
            if (be instanceof WindmillRotorBlockEntity rotor) {
                stack.add(rotor);
                if (isStackedNeighbor(level, cur, facing, axlePos)) {
                    cur = cur.relative(axlePos);
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        if (stack.isEmpty()) return;

        WindmillRotorBlockEntity headRotor = stack.get(0);
        WindmillRotorBlockEntity tailRotor = stack.get(stack.size() - 1);

        BlockPos negTargetPos = headRotor.getBlockPos().relative(axleNeg);
        EnergyHandler negTarget = level.getCapability(Capabilities.Energy.BLOCK, negTargetPos, axlePos);

        BlockPos posTargetPos = tailRotor.getBlockPos().relative(axlePos);
        EnergyHandler posTarget = level.getCapability(Capabilities.Energy.BLOCK, posTargetPos, axleNeg);

        int maxTransfer = PowerGridConfig.CONFIG.windmillMaxTransfer.get();

        // Push energy from interior rotors towards terminals
        for (int i = 0; i < stack.size(); i++) {
            WindmillRotorBlockEntity rotor = stack.get(i);
            int stored = rotor.energyStorage.getAmountAsInt();
            if (stored <= 0) continue;

            if (negTarget != null && posTarget != null) {
                if (i < stack.size() / 2) {
                    pushTowards(rotor, headRotor, negTarget, maxTransfer);
                } else {
                    pushTowards(rotor, tailRotor, posTarget, maxTransfer);
                }
            } else if (negTarget != null) {
                pushTowards(rotor, headRotor, negTarget, maxTransfer);
            } else if (posTarget != null) {
                pushTowards(rotor, tailRotor, posTarget, maxTransfer);
            } else {
                pushToPerpendicularNeighbors(level, rotor, facing, maxTransfer);
            }
        }
    }

    private static void pushTowards(WindmillRotorBlockEntity source, WindmillRotorBlockEntity terminal, EnergyHandler target, int maxTransfer) {
        int available = source.energyStorage.getAmountAsInt();
        if (available <= 0) return;

        int toPush = Math.min(available, maxTransfer);
        int acceptedSim;
        try (var simTx = Transaction.openRoot()) {
            acceptedSim = target.insert(toPush, simTx);
        }

        if (acceptedSim > 0) {
            try (var tx = Transaction.openRoot()) {
                int extracted = source.energyStorage.extract(acceptedSim, tx);
                if (extracted > 0) {
                    int accepted = target.insert(extracted, tx);
                    if (accepted == extracted) {
                        tx.commit();
                        source.setChanged();
                    }
                }
            }
        }
    }

    private static void pushToPerpendicularNeighbors(Level level, WindmillRotorBlockEntity rotor, Direction facing, int maxTransfer) {
        int available = rotor.energyStorage.getAmountAsInt();
        if (available <= 0) return;

        BlockPos pos = rotor.getBlockPos();
        Direction.Axis axleAxis = facing.getAxis();

        for (Direction side : Direction.values()) {
            if (side.getAxis() == axleAxis) continue;

            BlockPos targetPos = pos.relative(side);
            if (!level.isLoaded(targetPos)) continue;

            EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, targetPos, side.getOpposite());
            if (target != null) {
                int toPush = Math.min(rotor.energyStorage.getAmountAsInt(), maxTransfer);
                int acceptedSim;
                try (var simTx = Transaction.openRoot()) {
                    acceptedSim = target.insert(toPush, simTx);
                }
                if (acceptedSim > 0) {
                    try (var tx = Transaction.openRoot()) {
                        int extracted = rotor.energyStorage.extract(acceptedSim, tx);
                        if (extracted > 0) {
                            int accepted = target.insert(extracted, tx);
                            if (accepted == extracted) {
                                tx.commit();
                                rotor.setChanged();
                                if (rotor.energyStorage.getAmountAsInt() <= 0) break;
                            }
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
        output.putInt("groundClearance", this.groundClearance);
        output.putInt("feGenerated", this.feGeneratedLastTick);
        output.putBoolean("isSpinning", this.isSpinning);
        output.putFloat("spinSpeed", this.spinSpeed);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.energyStorage.set(input.getIntOr("storedEnergy", 0));
        this.groundClearance = input.getIntOr("groundClearance", 0);
        this.feGeneratedLastTick = input.getIntOr("feGenerated", 0);
        this.isSpinning = input.getBooleanOr("isSpinning", false);
        this.spinSpeed = input.getFloatOr("spinSpeed", 0.0f);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("storedEnergy", this.energyStorage.getAmountAsInt());
        tag.putInt("groundClearance", this.groundClearance);
        tag.putInt("feGenerated", this.feGeneratedLastTick);
        tag.putBoolean("isSpinning", this.isSpinning);
        tag.putFloat("spinSpeed", this.spinSpeed);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

package com.powergrid.block.entity;

import com.powergrid.PowerGridMod;
import com.powergrid.block.WaterWheelPart;
import com.powergrid.block.WoodenWaterWheelBlock;
import com.powergrid.config.PowerGridConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WoodenWaterWheelBlockEntity extends BlockEntity {
    public static final int DEFAULT_CAPACITY = 10_000;
    public static final int DEFAULT_MAX_TRANSFER = 2_000;

    public final SimpleEnergyHandler energyStorage = new SimpleEnergyHandler(DEFAULT_CAPACITY, DEFAULT_MAX_TRANSFER, DEFAULT_MAX_TRANSFER);

    // Dynamic kinetic state
    private int waterDropHeight = 0;
    private int feGeneratedLastTick = 0;
    private boolean isSpinning = false;
    private float spinDirection = 1.0f; // +1.0f (forward/clockwise), -1.0f (reverse)

    public WoodenWaterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(PowerGridMod.WOODEN_WATER_WHEEL_BE.get(), pos, state);
    }

    public EnergyHandler getEnergyStorage(@Nullable Direction side) {
        return this.energyStorage;
    }

    public int getWaterDropHeight() {
        return this.waterDropHeight;
    }

    public int getFeGeneratedLastTick() {
        return this.feGeneratedLastTick;
    }

    public boolean isSpinning() {
        return this.isSpinning;
    }

    public float getSpinDirection() {
        return this.spinDirection;
    }

    public float getSpinSpeedDegrees() {
        int drop = Math.max(1, this.waterDropHeight);
        float baseSpeed = 2.0f + (drop - 1) * 1.5f; // 2.0 to 8.0 degrees/tick based on drop height
        return this.spinDirection * baseSpeed;
    }

    public void clientTick() {
        // Maintained for compatibility; frame-rate independent rendering uses gameTime + partialTicks
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(WoodenWaterWheelBlock.FACING);

        // 1. Calculate water drop height & flow torque
        WaterFlowResult flow = calculateWaterDropHeight(level, pos, facing);
        int drop = flow.dropHeight;
        this.waterDropHeight = drop;
        this.spinDirection = flow.flowDirection;

        int fePerDrop = PowerGridConfig.CONFIG.waterWheelFePerDrop.get();
        if (drop > 0) {
            this.isSpinning = true;
            this.feGeneratedLastTick = drop * fePerDrop;
            try (var tx = Transaction.openRoot()) {
                this.energyStorage.insert(this.feGeneratedLastTick, tx);
                tx.commit();
            }

            if (!state.getValue(WoodenWaterWheelBlock.OPERATING)) {
                level.setBlock(pos, state.setValue(WoodenWaterWheelBlock.OPERATING, true), 3);
            }
        } else {
            this.isSpinning = false;
            this.feGeneratedLastTick = 0;

            if (state.getValue(WoodenWaterWheelBlock.OPERATING)) {
                level.setBlock(pos, state.setValue(WoodenWaterWheelBlock.OPERATING, false), 3);
            }
        }

        // 2. Axle Stack Coordinated Energy Distribution
        Direction axleNeg = (facing.getAxis() == Direction.Axis.Z) ? Direction.NORTH : Direction.WEST;
        Direction axlePos = (facing.getAxis() == Direction.Axis.Z) ? Direction.SOUTH : Direction.EAST;

        // Coordinate axle stack starting from the negative-most wheel (stack leader)
        if (!isStackedNeighbor(level, pos, facing, axleNeg)) {
            processAxleStack(level, pos, facing, axleNeg, axlePos);
        }

        // Periodic sync to client
        if (level.getGameTime() % 20 == 0) {
            level.sendBlockUpdated(pos, state, state, 2);
        }
    }

    private static boolean isStackedNeighbor(Level level, BlockPos pos, Direction facing, Direction dir) {
        BlockPos neighborPos = pos.relative(dir);
        if (!level.isLoaded(neighborPos)) return false;
        BlockState neighborState = level.getBlockState(neighborPos);
        if (neighborState.getBlock() instanceof WoodenWaterWheelBlock) {
            return neighborState.getValue(WoodenWaterWheelBlock.FACING).getAxis() == facing.getAxis()
                    && neighborState.getValue(WoodenWaterWheelBlock.PART) == WaterWheelPart.CENTER;
        }
        return false;
    }

    private void processAxleStack(Level level, BlockPos startPos, Direction facing, Direction axleNeg, Direction axlePos) {
        // Collect all wheels in this contiguous axle stack
        List<WoodenWaterWheelBlockEntity> stack = new ArrayList<>();
        BlockPos cur = startPos;
        while (cur != null && level.isLoaded(cur)) {
            BlockEntity be = level.getBlockEntity(cur);
            if (be instanceof WoodenWaterWheelBlockEntity wheel) {
                stack.add(wheel);
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

        WoodenWaterWheelBlockEntity headWheel = stack.get(0);
        WoodenWaterWheelBlockEntity tailWheel = stack.get(stack.size() - 1);

        // Check external energy receivers at both ends of the axle
        BlockPos negTargetPos = headWheel.getBlockPos().relative(axleNeg);
        EnergyHandler negTarget = level.getCapability(Capabilities.Energy.BLOCK, negTargetPos, axlePos);

        BlockPos posTargetPos = tailWheel.getBlockPos().relative(axlePos);
        EnergyHandler posTarget = level.getCapability(Capabilities.Energy.BLOCK, posTargetPos, axleNeg);

        int maxTransfer = PowerGridConfig.CONFIG.waterWheelMaxTransfer.get();

        // Push energy out towards tail (positive end) if an external receiver exists
        if (posTarget != null) {
            pushStackEnergyTo(stack, posTarget, maxTransfer, false);
        }

        // Push energy out towards head (negative end) if an external receiver exists
        if (negTarget != null) {
            pushStackEnergyTo(stack, negTarget, maxTransfer, true);
        }

        // Also check perpendicular faces of individual wheels for direct attachments
        for (WoodenWaterWheelBlockEntity wheel : stack) {
            pushToPerpendicularNeighbors(level, wheel, facing, maxTransfer);
        }
    }

    private static void pushStackEnergyTo(List<WoodenWaterWheelBlockEntity> stack, EnergyHandler target, int maxTransfer, boolean fromHead) {
        int listSize = stack.size();
        for (int i = 0; i < listSize; i++) {
            WoodenWaterWheelBlockEntity wheel = fromHead ? stack.get(i) : stack.get(listSize - 1 - i);
            int available = wheel.energyStorage.getAmountAsInt();
            if (available <= 0) continue;

            int toPush = Math.min(available, maxTransfer);
            int acceptedSim;
            try (var simTx = Transaction.openRoot()) {
                acceptedSim = target.insert(toPush, simTx);
            }

            if (acceptedSim > 0) {
                try (var tx = Transaction.openRoot()) {
                    int extracted = wheel.energyStorage.extract(acceptedSim, tx);
                    if (extracted > 0) {
                        int accepted = target.insert(extracted, tx);
                        if (accepted == extracted) {
                            tx.commit();
                            wheel.setChanged();
                        }
                    }
                }
            }
        }
    }

    private static void pushToPerpendicularNeighbors(Level level, WoodenWaterWheelBlockEntity wheel, Direction facing, int maxTransfer) {
        int available = wheel.energyStorage.getAmountAsInt();
        if (available <= 0) return;

        BlockPos pos = wheel.getBlockPos();
        Direction.Axis axleAxis = facing.getAxis();

        for (Direction side : Direction.values()) {
            if (side.getAxis() == axleAxis) continue; // Skip axle faces already handled by stack

            BlockPos targetPos = pos.relative(side);
            if (!level.isLoaded(targetPos)) continue;

            EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, targetPos, side.getOpposite());
            if (target != null) {
                int toPush = Math.min(wheel.energyStorage.getAmountAsInt(), maxTransfer);
                int acceptedSim;
                try (var simTx = Transaction.openRoot()) {
                    acceptedSim = target.insert(toPush, simTx);
                }
                if (acceptedSim > 0) {
                    try (var tx = Transaction.openRoot()) {
                        int extracted = wheel.energyStorage.extract(acceptedSim, tx);
                        if (extracted > 0) {
                            int accepted = target.insert(extracted, tx);
                            if (accepted == extracted) {
                                tx.commit();
                                wheel.setChanged();
                                if (wheel.energyStorage.getAmountAsInt() <= 0) break;
                            }
                        }
                    }
                }
            }
        }
    }

    public record WaterFlowResult(int dropHeight, float flowDirection) {}

    public static WaterFlowResult calculateWaterDropHeight(Level level, BlockPos centerPos, Direction facing) {
        Direction hDir = WoodenWaterWheelBlock.getHorizontalDiameterDirection(facing);

        boolean hasFlowingWater = false;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        double netFlowH = 0.0;
        int positiveSideCount = 0;
        int negativeSideCount = 0;

        // Check the 3x3 wheel footprint and immediate water intake/outflow perimeter
        for (int dh = -2; dh <= 2; dh++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (Math.abs(dh) == 2 && Math.abs(dy) == 2) continue; // skip far diagonal corners

                BlockPos checkPos = centerPos.above(dy).relative(hDir, dh);
                if (!level.isLoaded(checkPos)) continue;

                FluidState fs = level.getFluidState(checkPos);
                if (!fs.isEmpty() && fs.is(FluidTags.WATER)) {
                    boolean flowing = !fs.isSource()
                            || (fs.hasProperty(FlowingFluid.FALLING) && fs.getValue(FlowingFluid.FALLING))
                            || fs.getFlow(level, checkPos).lengthSqr() > 1e-4;
                    if (flowing) {
                        hasFlowingWater = true;
                        int y = checkPos.getY();
                        if (y < minY) minY = y;
                        if (y > maxY) maxY = y;

                        Vec3 flowVec = fs.getFlow(level, checkPos);
                        double componentH = (hDir.getAxis() == Direction.Axis.X)
                                ? (flowVec.x * hDir.getStepX())
                                : (flowVec.z * hDir.getStepZ());
                        netFlowH += componentH;

                        if (dh > 0) positiveSideCount++;
                        else if (dh < 0) negativeSideCount++;
                    }
                }
            }
        }

        if (!hasFlowingWater) {
            return new WaterFlowResult(0, 1.0f); // Static standing water or dry -> zero torque
        }

        // Check upward waterfall column plunging into top perimeter of the wheel
        int maxDrop = PowerGridConfig.CONFIG.waterWheelMaxDrop.get();
        for (int dh = -1; dh <= 1; dh++) {
            BlockPos topCol = centerPos.above(2).relative(hDir, dh);
            int currentY = topCol.getY();
            while (currentY - minY < maxDrop && currentY < level.getMaxY()) {
                BlockPos abovePos = new BlockPos(topCol.getX(), currentY, topCol.getZ());
                if (!level.isLoaded(abovePos)) break;

                FluidState fs = level.getFluidState(abovePos);
                if (!fs.isEmpty() && fs.is(FluidTags.WATER)) {
                    boolean flowing = !fs.isSource()
                            || (fs.hasProperty(FlowingFluid.FALLING) && fs.getValue(FlowingFluid.FALLING))
                            || fs.getFlow(level, abovePos).lengthSqr() > 1e-4;
                    if (flowing) {
                        if (currentY > maxY) maxY = currentY;
                        currentY++;
                        if (dh > 0) positiveSideCount += 2;
                        else if (dh < 0) negativeSideCount += 2;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        int dropLevels = (maxY - minY) + 1;
        int clampedDrop = Math.clamp(dropLevels, 1, maxDrop);

        // Determine rotation direction based on net horizontal flow or water dropping side
        float dir = 1.0f;
        if (Math.abs(netFlowH) > 0.05) {
            dir = netFlowH >= 0 ? 1.0f : -1.0f;
        } else if (negativeSideCount > positiveSideCount) {
            dir = -1.0f;
        }

        return new WaterFlowResult(clampedDrop, dir);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("storedEnergy", this.energyStorage.getAmountAsInt());
        output.putInt("waterDropHeight", this.waterDropHeight);
        output.putInt("feGenerated", this.feGeneratedLastTick);
        output.putBoolean("isSpinning", this.isSpinning);
        output.putFloat("spinDirection", this.spinDirection);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.energyStorage.set(input.getIntOr("storedEnergy", 0));
        this.waterDropHeight = input.getIntOr("waterDropHeight", 0);
        this.feGeneratedLastTick = input.getIntOr("feGenerated", 0);
        this.isSpinning = input.getBooleanOr("isSpinning", false);
        this.spinDirection = input.getFloatOr("spinDirection", 1.0f);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("storedEnergy", this.energyStorage.getAmountAsInt());
        tag.putInt("waterDropHeight", this.waterDropHeight);
        tag.putInt("feGenerated", this.feGeneratedLastTick);
        tag.putBoolean("isSpinning", this.isSpinning);
        tag.putFloat("spinDirection", this.spinDirection);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

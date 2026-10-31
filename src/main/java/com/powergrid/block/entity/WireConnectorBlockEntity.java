package com.powergrid.block.entity;

import com.powergrid.PowerGridMod;
import com.powergrid.block.WireConnectorBlock;
import com.powergrid.grid.IWireNode;
import com.powergrid.grid.WireConnection;
import com.powergrid.grid.WireType;
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

import java.util.ArrayList;
import java.util.List;

public class WireConnectorBlockEntity extends BlockEntity implements IWireNode {
    private final WireType wireType;
    private final List<WireConnection> connections = new ArrayList<>();
    public final SimpleEnergyHandler energyStorage;

    public WireConnectorBlockEntity(BlockPos pos, BlockState state, WireType wireType) {
        super(PowerGridMod.WIRE_CONNECTOR_BE.get(), pos, state);
        this.wireType = wireType;
        int rate = wireType.getTransferRate();
        this.energyStorage = new SimpleEnergyHandler(rate * 4, rate, rate);
    }

    public WireConnectorBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, WireType.LV);
    }

    @Override
    public WireType getSupportedWireType() {
        return this.wireType;
    }

    @Override
    public int getMaxConnections() {
        return 4;
    }

    @Override
    public boolean canConnectWire(WireType type, BlockPos otherPos) {
        if (type != this.wireType) return false;
        if (otherPos != null && !otherPos.equals(this.worldPosition)) {
            double distSq = this.worldPosition.distSqr(otherPos);
            if (distSq > (double) (type.getMaxDistance() * type.getMaxDistance())) return false;
            for (WireConnection conn : connections) {
                if (conn.getTargetPos().equals(otherPos)) return false;
            }
        }
        return connections.size() < getMaxConnections();
    }

    @Override
    public void addConnection(BlockPos targetPos, WireType type, double distance) {
        this.connections.add(new WireConnection(targetPos, type, distance));
        setChanged();
    }

    @Override
    public void removeConnection(BlockPos targetPos) {
        this.connections.removeIf(conn -> conn.getTargetPos().equals(targetPos));
        setChanged();
    }

    @Override
    public void clearAllConnections() {
        this.connections.clear();
        setChanged();
    }

    @Override
    public List<WireConnection> getConnections() {
        return this.connections;
    }

    @Override
    public BlockPos getNodePos() {
        return this.worldPosition;
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(WireConnectorBlock.FACING);
        Direction attachedFace = facing.getOpposite();
        BlockPos attachedPos = pos.relative(attachedFace);

        // Exchange energy with attached machine/generator/storage
        EnergyHandler neighborEnergy = level.getCapability(Capabilities.Energy.BLOCK, attachedPos, facing);
        if (neighborEnergy != null) {
            // Pull energy from neighbor if connector has room
            int room = this.energyStorage.getCapacityAsInt() - this.energyStorage.getAmountAsInt();
            if (room > 0) {
                try (var tx = Transaction.openRoot()) {
                    int extracted = neighborEnergy.extract(Math.min(room, this.wireType.getTransferRate()), tx);
                    if (extracted > 0) {
                        int inserted = this.energyStorage.insert(extracted, tx);
                        if (inserted == extracted) {
                            tx.commit();
                            setChanged();
                        }
                    }
                }
            }

            // Push energy to neighbor if connector has energy
            int stored = this.energyStorage.getAmountAsInt();
            if (stored > 0) {
                int toPush = Math.min(stored, this.wireType.getTransferRate());
                try (var tx = Transaction.openRoot()) {
                    int extracted = this.energyStorage.extract(toPush, tx);
                    if (extracted > 0) {
                        int accepted = neighborEnergy.insert(extracted, tx);
                        if (accepted == extracted) {
                            tx.commit();
                            setChanged();
                        }
                    }
                }
            }
        }

        // Transmit energy through connected wire lines
        int currentStored = this.energyStorage.getAmountAsInt();
        if (currentStored > 0 && !connections.isEmpty()) {
            int toDistribute = Math.min(currentStored, this.wireType.getTransferRate());
            int perConn = toDistribute / connections.size();
            if (perConn > 0) {
                for (WireConnection conn : connections) {
                    if (level.getBlockEntity(conn.getTargetPos()) instanceof WireConnectorBlockEntity targetBE) {
                        int available = targetBE.energyStorage.getCapacityAsInt() - targetBE.energyStorage.getAmountAsInt();
                        if (available > 0) {
                            int send = Math.min(perConn, available);
                            float lossPercent = (float) conn.getDistance() * this.wireType.getLossPerBlock();
                            int loss = Math.round(send * lossPercent);
                            int netReceive = Math.max(0, send - loss);

                            try (var tx = Transaction.openRoot()) {
                                int extracted = this.energyStorage.extract(send, tx);
                                if (extracted > 0) {
                                    int inserted = targetBE.energyStorage.insert(netReceive, tx);
                                    if (inserted == netReceive) {
                                        tx.commit();
                                        targetBE.setChanged();
                                        setChanged();
                                    }
                                }
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
        output.putInt("connCount", this.connections.size());
        for (int i = 0; i < this.connections.size(); i++) {
            WireConnection c = this.connections.get(i);
            output.putInt("c_x_" + i, c.getTargetPos().getX());
            output.putInt("c_y_" + i, c.getTargetPos().getY());
            output.putInt("c_z_" + i, c.getTargetPos().getZ());
            output.putString("c_type_" + i, c.getWireType().getSerializedName());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        int stored = input.getIntOr("storedEnergy", 0);
        this.energyStorage.set(stored);
        this.connections.clear();
        int count = input.getIntOr("connCount", 0);
        for (int i = 0; i < count; i++) {
            int x = input.getIntOr("c_x_" + i, 0);
            int y = input.getIntOr("c_y_" + i, 0);
            int z = input.getIntOr("c_z_" + i, 0);
            String typeStr = input.getStringOr("c_type_" + i, "lv");
            WireType type = typeStr.equals("hv") ? WireType.HV : typeStr.equals("mv") ? WireType.MV : WireType.LV;
            BlockPos target = new BlockPos(x, y, z);
            this.connections.add(new WireConnection(target, type, Math.sqrt(this.worldPosition.distSqr(target))));
        }
    }
}

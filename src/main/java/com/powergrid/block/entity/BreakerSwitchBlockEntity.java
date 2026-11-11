package com.powergrid.block.entity;

import com.powergrid.PowerGridMod;
import com.powergrid.block.BreakerSwitchBlock;
import com.powergrid.grid.ConnectorMode;
import com.powergrid.grid.GridNetworkSavedData;
import com.powergrid.grid.IWireNode;
import com.powergrid.grid.WireConnection;
import com.powergrid.grid.WireType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class BreakerSwitchBlockEntity extends BlockEntity implements IWireNode {
    private final List<WireConnection> connections = new ArrayList<>();

    public BreakerSwitchBlockEntity(BlockPos pos, BlockState state) {
        super(PowerGridMod.BREAKER_SWITCH_BE.get(), pos, state);
    }

    public boolean isCircuitClosed() {
        return !getBlockState().getValue(BreakerSwitchBlock.OPEN);
    }

    private void syncToClient() {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level instanceof ServerLevel serverLevel) {
            GridNetworkSavedData savedData = GridNetworkSavedData.get(serverLevel);
            boolean changed = savedData.syncNode(this.worldPosition, GridNetworkSavedData.NodeType.BREAKER,
                    getSupportedWireType(), this.connections, ConnectorMode.AUTO, isCircuitClosed(), null);
            if (changed) {
                java.util.Set<BlockPos> removals = savedData.getAndClearPendingRemovals(this.worldPosition);
                this.connections.removeIf(c -> removals.contains(c.getTargetPos()));
                setChanged();
            }
        }
    }

    @Override
    public WireType getSupportedWireType() {
        return WireType.MV;
    }

    @Override
    public boolean isWireTypeSupported(WireType type) {
        return true; // Heavy duty industrial breaker switch supports LV, MV, and HV circuits
    }

    @Override
    public int getMaxConnections() {
        return 2; // Breaker acts as an in-line switch between 2 nodes
    }

    @Override
    public boolean canConnectWire(WireType type, BlockPos otherPos) {
        if (!isWireTypeSupported(type)) return false;
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
        this.connections.removeIf(conn -> conn.getTargetPos().equals(targetPos));
        this.connections.add(new WireConnection(targetPos, type, distance));
        setChanged();
        syncToClient();
        if (this.level instanceof ServerLevel serverLevel) {
            GridNetworkSavedData.get(serverLevel).addWireConnection(this.worldPosition, targetPos, type, distance);
        }
    }

    @Override
    public void removeConnection(BlockPos targetPos) {
        this.connections.removeIf(conn -> conn.getTargetPos().equals(targetPos));
        setChanged();
        syncToClient();
        if (this.level instanceof ServerLevel serverLevel) {
            boolean targetLoaded = serverLevel.hasChunkAt(targetPos);
            GridNetworkSavedData.get(serverLevel).removeWireConnection(this.worldPosition, targetPos, true, targetLoaded);
        }
    }

    @Override
    public void clearAllConnections() {
        if (this.level instanceof ServerLevel serverLevel) {
            GridNetworkSavedData savedData = GridNetworkSavedData.get(serverLevel);
            for (WireConnection conn : this.connections) {
                boolean targetLoaded = serverLevel.hasChunkAt(conn.getTargetPos());
                savedData.removeWireConnection(this.worldPosition, conn.getTargetPos(), true, targetLoaded);
            }
        }
        this.connections.clear();
        setChanged();
        syncToClient();
    }

    @Override
    public List<WireConnection> getConnections() {
        return this.connections;
    }

    @Override
    public BlockPos getNodePos() {
        return this.worldPosition;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level == null) return;

        if (this.level instanceof ServerLevel serverLevel) {
            GridNetworkSavedData.get(serverLevel).removeNode(pos);
        }

        List<WireConnection> toSever = new ArrayList<>(this.connections);
        this.connections.clear();
        setChanged();

        for (WireConnection conn : toSever) {
            BlockPos otherPos = conn.getTargetPos();
            if (this.level.isLoaded(otherPos)) {
                BlockEntity otherBE = this.level.getBlockEntity(otherPos);
                if (otherBE instanceof IWireNode otherNode) {
                    otherNode.removeConnection(pos);
                    this.level.sendBlockUpdated(otherPos, this.level.getBlockState(otherPos), this.level.getBlockState(otherPos), 3);
                }
            }

            // Drop wire coils in survival
            if (this.level instanceof ServerLevel serverLevel) {
                Block.popResource(serverLevel, pos, new ItemStack(conn.getWireType().getCoilItem()));
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
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
        this.connections.clear();
        int count = input.getIntOr("connCount", 0);
        for (int i = 0; i < count; i++) {
            int x = input.getIntOr("c_x_" + i, 0);
            int y = input.getIntOr("c_y_" + i, 0);
            int z = input.getIntOr("c_z_" + i, 0);
            String typeStr = input.getStringOr("c_type_" + i, "mv");
            WireType type = typeStr.equals("hv") ? WireType.HV : typeStr.equals("mv") ? WireType.MV : WireType.LV;
            BlockPos target = new BlockPos(x, y, z);
            this.connections.add(new WireConnection(target, type, Math.sqrt(this.worldPosition.distSqr(target))));
        }
    }
}

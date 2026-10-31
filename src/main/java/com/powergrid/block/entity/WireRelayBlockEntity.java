package com.powergrid.block.entity;

import com.powergrid.PowerGridMod;
import com.powergrid.grid.IWireNode;
import com.powergrid.grid.WireConnection;
import com.powergrid.grid.WireType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class WireRelayBlockEntity extends BlockEntity implements IWireNode {
    private final WireType wireType;
    private final List<WireConnection> connections = new ArrayList<>();

    public WireRelayBlockEntity(BlockPos pos, BlockState state, WireType wireType) {
        super(PowerGridMod.WIRE_RELAY_BE.get(), pos, state);
        this.wireType = wireType;
    }

    public WireRelayBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, WireType.LV);
    }

    @Override
    public WireType getSupportedWireType() {
        return this.wireType;
    }

    @Override
    public int getMaxConnections() {
        return 8;
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
            String typeStr = input.getStringOr("c_type_" + i, "lv");
            WireType type = typeStr.equals("hv") ? WireType.HV : typeStr.equals("mv") ? WireType.MV : WireType.LV;
            BlockPos target = new BlockPos(x, y, z);
            this.connections.add(new WireConnection(target, type, Math.sqrt(this.worldPosition.distSqr(target))));
        }
    }
}

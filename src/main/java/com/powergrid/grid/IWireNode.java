package com.powergrid.grid;

import net.minecraft.core.BlockPos;

import java.util.List;

public interface IWireNode {
    WireType getSupportedWireType();

    boolean canConnectWire(WireType type, BlockPos otherPos);

    void addConnection(BlockPos targetPos, WireType type, double distance);

    void removeConnection(BlockPos targetPos);

    void clearAllConnections();

    List<WireConnection> getConnections();

    BlockPos getNodePos();
}

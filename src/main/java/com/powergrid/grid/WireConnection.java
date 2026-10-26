package com.powergrid.grid;

import net.minecraft.core.BlockPos;

public class WireConnection {
    private final BlockPos targetPos;
    private final WireType wireType;
    private final double distance;

    public WireConnection(BlockPos targetPos, WireType wireType, double distance) {
        this.targetPos = targetPos;
        this.wireType = wireType;
        this.distance = distance;
    }

    public BlockPos getTargetPos() {
        return targetPos;
    }

    public WireType getWireType() {
        return wireType;
    }

    public double getDistance() {
        return distance;
    }
}

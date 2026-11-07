package com.powergrid.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

public enum WindmillRotorPart implements StringRepresentable {
    CENTER("center", 0, 0),
    TOP("top", 0, 1),
    BOTTOM("bottom", 0, -1),
    LEFT("left", -1, 0),
    RIGHT("right", 1, 0),
    TOP_LEFT("top_left", -1, 1),
    TOP_RIGHT("top_right", 1, 1),
    BOTTOM_LEFT("bottom_left", -1, -1),
    BOTTOM_RIGHT("bottom_right", 1, -1);

    private final String name;
    private final int offsetH;
    private final int offsetY;

    WindmillRotorPart(String name, int offsetH, int offsetY) {
        this.name = name;
        this.offsetH = offsetH;
        this.offsetY = offsetY;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public int getOffsetH() {
        return this.offsetH;
    }

    public int getOffsetY() {
        return this.offsetY;
    }

    public static Direction getHorizontalDiameterDirection(Direction facing) {
        return facing.getAxis() == Direction.Axis.Z ? Direction.EAST : Direction.SOUTH;
    }

    public static BlockPos getCenterPos(BlockPos currentPos, Direction facing, WindmillRotorPart part) {
        if (part == CENTER) return currentPos;
        Direction diameterDir = getHorizontalDiameterDirection(facing);
        return currentPos.below(part.getOffsetY()).relative(diameterDir, -part.getOffsetH());
    }

    public static BlockPos getPartPos(BlockPos centerPos, Direction facing, WindmillRotorPart part) {
        if (part == CENTER) return centerPos;
        Direction diameterDir = getHorizontalDiameterDirection(facing);
        return centerPos.above(part.getOffsetY()).relative(diameterDir, part.getOffsetH());
    }
}

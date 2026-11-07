package com.powergrid.block;

import com.powergrid.client.renderer.WoodenWaterWheelModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WoodenWaterWheelTest {

    @Test
    @DisplayName("Verify diameter direction for both horizontal axes")
    void testDiameterDirection() {
        assertEquals(Direction.EAST, WaterWheelPart.getHorizontalDiameterDirection(Direction.NORTH));
        assertEquals(Direction.EAST, WaterWheelPart.getHorizontalDiameterDirection(Direction.SOUTH));

        assertEquals(Direction.SOUTH, WaterWheelPart.getHorizontalDiameterDirection(Direction.EAST));
        assertEquals(Direction.SOUTH, WaterWheelPart.getHorizontalDiameterDirection(Direction.WEST));
    }

    @Test
    @DisplayName("Verify all 9 parts calculate correct positions and invert cleanly back to center")
    void testPartPositionCalculations() {
        BlockPos centerPos = new BlockPos(100, 64, -200);

        for (Direction facing : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            for (WaterWheelPart part : WaterWheelPart.values()) {
                BlockPos partPos = WaterWheelPart.getPartPos(centerPos, facing, part);
                assertNotNull(partPos);

                // Verify relative vertical offset
                assertEquals(centerPos.getY() + part.getOffsetY(), partPos.getY());

                // Verify that reversing using the part's offsets returns the exact centerPos
                Direction diameterDir = WaterWheelPart.getHorizontalDiameterDirection(facing);
                BlockPos calculatedCenter = partPos.below(part.getOffsetY()).relative(diameterDir, -part.getOffsetH());
                assertEquals(centerPos, calculatedCenter, "Failed inversion for part " + part + " with facing " + facing);
            }
        }
    }

    @Test
    @DisplayName("Verify 0.5px UV padding formula produces strictly valid inset ranges")
    void testUVRectPadding() {
        WoodenWaterWheelModel.UVRect uv = WoodenWaterWheelModel.UVRect.of(0, 96, 64, 48);

        assertTrue(uv.u0() > 0.0f);
        assertTrue(uv.v0() > 0.0f);
        assertTrue(uv.u1() > uv.u0());
        assertTrue(uv.v1() > uv.v0());
        assertTrue(uv.u1() < 1.0f);
        assertTrue(uv.v1() < 1.0f);

        // Check half-texel inset math: (0 + 0.5) / 256
        assertEquals(0.5f / 256.0f, uv.u0(), 1e-6f);
        assertEquals(96.5f / 256.0f, uv.v0(), 1e-6f);
        assertEquals((64.0f - 0.5f) / 256.0f, uv.u1(), 1e-6f);
        assertEquals((96.0f + 48.0f - 0.5f) / 256.0f, uv.v1(), 1e-6f);
    }
}

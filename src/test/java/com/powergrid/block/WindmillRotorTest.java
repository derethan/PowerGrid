package com.powergrid.block;

import com.powergrid.client.renderer.WindmillRotorModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WindmillRotorTest {

    @Test
    @DisplayName("Verify diameter direction for both horizontal axes")
    void testDiameterDirection() {
        assertEquals(Direction.EAST, WindmillRotorPart.getHorizontalDiameterDirection(Direction.NORTH));
        assertEquals(Direction.EAST, WindmillRotorPart.getHorizontalDiameterDirection(Direction.SOUTH));

        assertEquals(Direction.SOUTH, WindmillRotorPart.getHorizontalDiameterDirection(Direction.EAST));
        assertEquals(Direction.SOUTH, WindmillRotorPart.getHorizontalDiameterDirection(Direction.WEST));
    }

    @Test
    @DisplayName("Verify all 9 parts calculate correct positions and invert cleanly back to center")
    void testPartPositionCalculations() {
        BlockPos centerPos = new BlockPos(50, 80, -120);

        for (Direction facing : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
            for (WindmillRotorPart part : WindmillRotorPart.values()) {
                BlockPos partPos = WindmillRotorPart.getPartPos(centerPos, facing, part);
                assertNotNull(partPos);

                // Verify relative vertical offset
                assertEquals(centerPos.getY() + part.getOffsetY(), partPos.getY());

                // Verify that reversing using getCenterPos returns the exact centerPos
                BlockPos calculatedCenter = WindmillRotorPart.getCenterPos(partPos, facing, part);
                assertEquals(centerPos, calculatedCenter, "Failed inversion for part " + part + " with facing " + facing);
            }
        }
    }

    @Test
    @DisplayName("Verify 0.5px UV padding formula produces strictly valid inset ranges")
    void testUVRectPadding() {
        WindmillRotorModel.UVRect uv = WindmillRotorModel.UVRect.of(0, 64, 64, 64);

        assertTrue(uv.u0() > 0.0f);
        assertTrue(uv.v0() > 0.0f);
        assertTrue(uv.u1() > uv.u0());
        assertTrue(uv.v1() > uv.v0());
        assertTrue(uv.u1() < 1.0f);
        assertTrue(uv.v1() < 1.0f);

        // Check half-texel inset math: (0 + 0.5) / 256
        assertEquals(0.5f / 256.0f, uv.u0(), 1e-6f);
        assertEquals(64.5f / 256.0f, uv.v0(), 1e-6f);
        assertEquals((64.0f - 0.5f) / 256.0f, uv.u1(), 1e-6f);
        assertEquals((128.0f - 0.5f) / 256.0f, uv.v1(), 1e-6f);
    }
}

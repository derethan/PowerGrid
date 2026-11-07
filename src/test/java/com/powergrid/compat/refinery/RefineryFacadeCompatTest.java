package com.powergrid.compat.refinery;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RefineryFacadeCompatTest {

    @Test
    @DisplayName("resolveTargetFace resolves outer facade boundary hits accurately")
    void testResolveTargetFace() {
        BlockPos pos = new BlockPos(10, 64, 10);

        // Near North boundary (z = 10.05, center z = 10.5, dz = -0.45)
        BlockHitResult hitNorth = new BlockHitResult(new Vec3(10.5, 64.5, 10.05), Direction.UP, pos, false);
        assertEquals(Direction.NORTH, RefineryFacadeCompat.resolveTargetFace(pos, hitNorth));

        // Near South boundary (z = 10.95, center z = 10.5, dz = +0.45)
        BlockHitResult hitSouth = new BlockHitResult(new Vec3(10.5, 64.5, 10.95), Direction.WEST, pos, false);
        assertEquals(Direction.SOUTH, RefineryFacadeCompat.resolveTargetFace(pos, hitSouth));

        // Near West boundary (x = 10.05, center x = 10.5, dx = -0.45)
        BlockHitResult hitWest = new BlockHitResult(new Vec3(10.05, 64.5, 10.5), Direction.UP, pos, false);
        assertEquals(Direction.WEST, RefineryFacadeCompat.resolveTargetFace(pos, hitWest));

        // Near East boundary (x = 10.95, center x = 10.5, dx = +0.45)
        BlockHitResult hitEast = new BlockHitResult(new Vec3(10.95, 64.5, 10.5), Direction.DOWN, pos, false);
        assertEquals(Direction.EAST, RefineryFacadeCompat.resolveTargetFace(pos, hitEast));

        // Near Up boundary (y = 10.95 in y=64 block -> 64.95, dy = +0.45)
        BlockHitResult hitUp = new BlockHitResult(new Vec3(10.5, 64.95, 10.5), Direction.NORTH, pos, false);
        assertEquals(Direction.UP, RefineryFacadeCompat.resolveTargetFace(pos, hitUp));

        // Near Down boundary (y = 64.05, dy = -0.45)
        BlockHitResult hitDown = new BlockHitResult(new Vec3(10.5, 64.05, 10.5), Direction.NORTH, pos, false);
        assertEquals(Direction.DOWN, RefineryFacadeCompat.resolveTargetFace(pos, hitDown));

        // Center core click (dx=0, dy=0, dz=0) falls back to hitResult.getDirection()
        BlockHitResult hitCore = new BlockHitResult(new Vec3(10.5, 64.5, 10.5), Direction.SOUTH, pos, false);
        assertEquals(Direction.SOUTH, RefineryFacadeCompat.resolveTargetFace(pos, hitCore));
    }

    @Test
    @DisplayName("FACADE_SHAPES covers all 6 directions with 2-pixel slabs")
    void testFacadeShapes() {
        for (Direction dir : Direction.values()) {
            VoxelShape shape = RefineryFacadeCompat.FACADE_SHAPES.get(dir);
            assertNotNull(shape, "Facade shape missing for direction " + dir);
            assertFalse(shape.isEmpty());
        }

        // Verify UP slab bounds (y: 14/16 to 16/16)
        VoxelShape upShape = RefineryFacadeCompat.FACADE_SHAPES.get(Direction.UP);
        assertEquals(0.0, upShape.min(Direction.Axis.X), 1e-4);
        assertEquals(1.0, upShape.max(Direction.Axis.X), 1e-4);
        assertEquals(14.0 / 16.0, upShape.min(Direction.Axis.Y), 1e-4);
        assertEquals(1.0, upShape.max(Direction.Axis.Y), 1e-4);

        // Verify DOWN slab bounds (y: 0 to 2/16)
        VoxelShape downShape = RefineryFacadeCompat.FACADE_SHAPES.get(Direction.DOWN);
        assertEquals(0.0, downShape.min(Direction.Axis.Y), 1e-4);
        assertEquals(2.0 / 16.0, downShape.max(Direction.Axis.Y), 1e-4);
    }

    @Test
    @DisplayName("CableFacadeData null checks work correctly")
    void testCableFacadeData() {
        CableFacadeData nullData = new CableFacadeData(null, false);
        assertFalse(nullData.isValid());
        assertFalse(nullData.hollow());
        assertNull(nullData.block());

        CableFacadeData hollowNullData = new CableFacadeData(null, true);
        assertFalse(hollowNullData.isValid());
        assertTrue(hollowNullData.hollow());
    }
}

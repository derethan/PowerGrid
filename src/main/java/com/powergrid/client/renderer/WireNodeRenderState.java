package com.powergrid.client.renderer;

import com.powergrid.grid.WireType;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class WireNodeRenderState extends BlockEntityRenderState {

    public static class RenderedWire {
        public final Vector3f start;
        public final Vector3f end;
        public final WireType wireType;
        public final double distance;

        public RenderedWire(Vector3f start, Vector3f end, WireType wireType, double distance) {
            this.start = start;
            this.end = end;
            this.wireType = wireType;
            this.distance = distance;
        }
    }

    public BlockPos blockPos = BlockPos.ZERO;
    public final List<RenderedWire> wires = new ArrayList<>();
}

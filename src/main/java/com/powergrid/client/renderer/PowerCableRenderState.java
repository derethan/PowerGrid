package com.powergrid.client.renderer;

import com.powergrid.compat.refinery.CableFacadeData;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.EnumMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class PowerCableRenderState extends BlockEntityRenderState {
    public final Map<Direction, CableFacadeData> facades = new EnumMap<>(Direction.class);
}

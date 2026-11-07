package com.powergrid.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WindmillRotorRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public boolean isOperating = false;
    public float spinSpeed = 0.0f;
    public long gameTime = 0;
    public float partialTicks = 0.0f;
}

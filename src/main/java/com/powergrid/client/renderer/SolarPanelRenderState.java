package com.powergrid.client.renderer;

import com.powergrid.block.SolarPanelTier;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SolarPanelRenderState extends BlockEntityRenderState {
    public SolarPanelTier tier = SolarPanelTier.STANDARD;
    public boolean north;
    public boolean south;
    public boolean east;
    public boolean west;
    public boolean northEast;
    public boolean northWest;
    public boolean southEast;
    public boolean southWest;
    public boolean active;
}

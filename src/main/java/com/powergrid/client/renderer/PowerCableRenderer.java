package com.powergrid.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.powergrid.block.entity.PowerCableBlockEntity;
import com.powergrid.compat.refinery.CableFacadeData;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PowerCableRenderer implements BlockEntityRenderer<PowerCableBlockEntity, PowerCableRenderState> {

    public PowerCableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public PowerCableRenderState createRenderState() {
        return new PowerCableRenderState();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return false;
    }

    @Override
    public AABB getRenderBoundingBox(PowerCableBlockEntity be) {
        return new AABB(be.getBlockPos());
    }

    @Override
    public void extractRenderState(
            PowerCableBlockEntity be,
            PowerCableRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        state.facades.clear();
        if (be.hasFacades()) {
            for (Direction dir : Direction.values()) {
                CableFacadeData data = be.getFacade(dir);
                if (data != null && data.isValid()) {
                    state.facades.put(dir, data);
                }
            }
        }
    }

    @Override
    public void submit(
            PowerCableRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        if (state.facades.isEmpty()) return;
        CableFacadeRenderer.submitFacades(state.facades, poseStack, submitNodeCollector, state.lightCoords);
    }
}

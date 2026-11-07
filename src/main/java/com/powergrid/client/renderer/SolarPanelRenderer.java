package com.powergrid.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.powergrid.block.SolarPanelBlock;
import com.powergrid.block.entity.SolarPanelBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SolarPanelRenderer implements BlockEntityRenderer<SolarPanelBlockEntity, SolarPanelRenderState> {
    private final SolarPanelModel model = new SolarPanelModel();

    public SolarPanelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SolarPanelRenderState createRenderState() {
        return new SolarPanelRenderState();
    }

    @Override
    public AABB getRenderBoundingBox(SolarPanelBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(0.5, 0.5, 0.5);
    }

    @Override
    public void extractRenderState(
            SolarPanelBlockEntity be,
            SolarPanelRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);

        state.tier = be.getTier();
        BlockState blockState = be.getBlockState();
        Level level = be.getLevel();
        BlockPos pos = be.getBlockPos();

        if (blockState.getBlock() instanceof SolarPanelBlock) {
            state.north = blockState.getValue(SolarPanelBlock.NORTH);
            state.south = blockState.getValue(SolarPanelBlock.SOUTH);
            state.east = blockState.getValue(SolarPanelBlock.EAST);
            state.west = blockState.getValue(SolarPanelBlock.WEST);
            state.active = blockState.getValue(SolarPanelBlock.ACTIVE);
        }

        if (level != null && blockState.getBlock() instanceof SolarPanelBlock myBlock) {
            state.north = level.getBlockState(pos.north()).is(myBlock);
            state.south = level.getBlockState(pos.south()).is(myBlock);
            state.east = level.getBlockState(pos.east()).is(myBlock);
            state.west = level.getBlockState(pos.west()).is(myBlock);

            state.northEast = level.getBlockState(pos.north().east()).is(myBlock);
            state.northWest = level.getBlockState(pos.north().west()).is(myBlock);
            state.southEast = level.getBlockState(pos.south().east()).is(myBlock);
            state.southWest = level.getBlockState(pos.south().west()).is(myBlock);
        }
    }

    @Override
    public void submit(
            SolarPanelRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.entitySolid(state.tier.getTexture()),
                (pose, consumer) -> this.model.render(
                        pose,
                        consumer,
                        state,
                        state.lightCoords,
                        OverlayTexture.NO_OVERLAY
                )
        );

        poseStack.popPose();
    }
}

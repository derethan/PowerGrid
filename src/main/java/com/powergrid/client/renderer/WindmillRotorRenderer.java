package com.powergrid.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.powergrid.block.WindmillRotorBlock;
import com.powergrid.block.entity.WindmillRotorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WindmillRotorRenderer implements BlockEntityRenderer<WindmillRotorBlockEntity, WindmillRotorRenderState> {
    private final WindmillRotorModel model;

    public WindmillRotorRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new WindmillRotorModel();
    }

    @Override
    public WindmillRotorRenderState createRenderState() {
        return new WindmillRotorRenderState();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(WindmillRotorBlockEntity be) {
        return new AABB(be.getBlockPos()).inflate(1.5);
    }

    @Override
    public void extractRenderState(
            WindmillRotorBlockEntity be,
            WindmillRotorRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);

        BlockState blockState = be.getBlockState();
        if (blockState.hasProperty(WindmillRotorBlock.FACING)) {
            state.facing = blockState.getValue(WindmillRotorBlock.FACING);
        } else {
            state.facing = Direction.NORTH;
        }

        boolean blockOperating = blockState.hasProperty(WindmillRotorBlock.OPERATING)
                && blockState.getValue(WindmillRotorBlock.OPERATING);
        state.isOperating = blockOperating || be.isSpinning();
        state.spinSpeed = be.getSpinSpeedDegrees();
        if (state.spinSpeed == 0.0f && state.isOperating) {
            state.spinSpeed = 3.0f; // Default active rotation speed if not yet synced
        }

        Level level = be.getLevel();
        state.gameTime = level != null ? level.getGameTime() : 0;
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(
            WindmillRotorRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        poseStack.pushPose();

        // Translate to the center of the 3x3x1 rotor
        poseStack.translate(0.5, 0.5, 0.5);

        // Align local Z axle with horizontal facing axis
        if (state.facing.getAxis() == Direction.Axis.X) {
            poseStack.rotateDegrees(Axis.YP, 90.0f);
        }

        // Apply continuous dynamic rotation when operating
        if (state.isOperating) {
            float animTicks = state.gameTime + state.partialTicks;
            float angleDegrees = (animTicks * state.spinSpeed) % 360.0f;
            poseStack.rotateDegrees(Axis.ZP, angleDegrees);
        }

        this.model.render(poseStack, submitNodeCollector, state.lightCoords);

        poseStack.popPose();
    }
}

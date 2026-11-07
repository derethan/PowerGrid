package com.powergrid.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.powergrid.block.BreakerSwitchBlock;
import com.powergrid.block.WireConnectorBlock;
import com.powergrid.grid.IWireNode;
import com.powergrid.grid.WireConnection;
import com.powergrid.grid.WireType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class WireNodeRenderer<T extends BlockEntity & IWireNode> implements BlockEntityRenderer<T, WireNodeRenderState> {
    public static final Identifier CABLE_TEXTURE = Identifier.fromNamespaceAndPath("powergrid", "textures/block/wire_cable.png");

    public WireNodeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public WireNodeRenderState createRenderState() {
        return new WireNodeRenderState();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(T be) {
        AABB box = new AABB(be.getBlockPos());
        for (WireConnection conn : be.getConnections()) {
            box = box.minmax(new AABB(conn.getTargetPos()));
        }
        return box.inflate(3.0);
    }

    @Override
    public void extractRenderState(
            T be,
            WireNodeRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderState.extractBase(be, state, breakProgress);
        state.blockPos = be.getBlockPos();
        state.wires.clear();

        Level level = be.getLevel();
        if (level == null) return;

        BlockPos selfPos = be.getBlockPos();
        BlockState selfState = be.getBlockState();
        Vector3f selfOffset = getTerminalOffset(selfState, be.getSupportedWireType());

        for (WireConnection conn : be.getConnections()) {
            BlockPos targetPos = conn.getTargetPos();

            // Canonical ordering: render from lower pos, or if remote is not loaded render locally
            if (selfPos.compareTo(targetPos) > 0 && level.isLoaded(targetPos)) {
                continue;
            }

            Vector3f targetOffset;
            if (level.isLoaded(targetPos)) {
                BlockState targetState = level.getBlockState(targetPos);
                BlockEntity targetBE = level.getBlockEntity(targetPos);
                WireType targetTier = (targetBE instanceof IWireNode targetNode)
                        ? targetNode.getSupportedWireType()
                        : conn.getWireType();
                targetOffset = getTerminalOffset(targetState, targetTier);
            } else {
                targetOffset = new Vector3f(0.5f, 0.6f, 0.5f);
            }

            float endX = (targetPos.getX() - selfPos.getX()) + targetOffset.x;
            float endY = (targetPos.getY() - selfPos.getY()) + targetOffset.y;
            float endZ = (targetPos.getZ() - selfPos.getZ()) + targetOffset.z;

            state.wires.add(new WireNodeRenderState.RenderedWire(
                    selfOffset,
                    new Vector3f(endX, endY, endZ),
                    conn.getWireType(),
                    conn.getDistance()
            ));
        }
    }

    @Override
    public void submit(
            WireNodeRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        if (state.wires.isEmpty()) return;

        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.entityCutout(CABLE_TEXTURE, false),
                (pose, consumer) -> {
                    for (WireNodeRenderState.RenderedWire wire : state.wires) {
                        renderCatenaryWire(pose, consumer, wire.start, wire.end, wire.wireType, state.lightCoords);
                    }
                }
        );
    }

    private static void renderCatenaryWire(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            Vector3f p0,
            Vector3f p1,
            WireType wireType,
            int light
    ) {
        float d = p0.distance(p1);
        if (d < 0.15f) return;

        int segments = Math.clamp((int) (d * 2.5f), 8, 36);
        float r = wireType.getCableRadius();
        int color = wireType.getColor();
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;

        Vector3f[] centers = new Vector3f[segments + 1];
        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            float x = p0.x + t * (p1.x - p0.x);
            float y = p0.y + t * (p1.y - p0.y);
            float z = p0.z + t * (p1.z - p0.z);

            // Natural gravitational sag
            float sag = -wireType.getSagFactor() * (float) Math.sin(t * Math.PI) * Math.min(3.5f, d * 0.35f);
            centers[i] = new Vector3f(x, y + sag, z);
        }

        Matrix4f matrix = pose.pose();

        for (int i = 0; i < segments; i++) {
            Vector3f cA = centers[i];
            Vector3f cB = centers[i + 1];

            Vector3f dir = new Vector3f(cB).sub(cA);
            if (dir.lengthSquared() < 1e-6f) continue;
            dir.normalize();

            Vector3f arb = Math.abs(dir.y) < 0.9f ? new Vector3f(0, 1, 0) : new Vector3f(1, 0, 0);
            Vector3f n1 = new Vector3f(dir).cross(arb).normalize().mul(r);
            Vector3f n2 = new Vector3f(dir).cross(n1).normalize().mul(r);

            // Cross quad 1 (oriented along n1, normal along n2)
            Vector3f pA1 = new Vector3f(cA).sub(n1);
            Vector3f pB1 = new Vector3f(cA).add(n1);
            Vector3f pC1 = new Vector3f(cB).add(n1);
            Vector3f pD1 = new Vector3f(cB).sub(n1);

            emitQuad(matrix, pose, consumer, pA1, pB1, pC1, pD1, n2.x, n2.y, n2.z, red, green, blue, light);

            // Cross quad 2 (oriented along n2, normal along n1)
            Vector3f pA2 = new Vector3f(cA).sub(n2);
            Vector3f pB2 = new Vector3f(cA).add(n2);
            Vector3f pC2 = new Vector3f(cB).add(n2);
            Vector3f pD2 = new Vector3f(cB).sub(n2);

            emitQuad(matrix, pose, consumer, pA2, pB2, pC2, pD2, n1.x, n1.y, n1.z, red, green, blue, light);
        }
    }

    private static void emitQuad(
            Matrix4f matrix,
            PoseStack.Pose pose,
            VertexConsumer consumer,
            Vector3f pA,
            Vector3f pB,
            Vector3f pC,
            Vector3f pD,
            float nx,
            float ny,
            float nz,
            int r,
            int g,
            int b,
            int light
    ) {
        consumer.addVertex(matrix, pA.x, pA.y, pA.z).setColor(r, g, b, 255).setUv(0.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(matrix, pB.x, pB.y, pB.z).setColor(r, g, b, 255).setUv(1.0f, 0.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(matrix, pC.x, pC.y, pC.z).setColor(r, g, b, 255).setUv(1.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
        consumer.addVertex(matrix, pD.x, pD.y, pD.z).setColor(r, g, b, 255).setUv(0.0f, 1.0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz);
    }

    private static Vector3f getTerminalOffset(BlockState state, WireType tier) {
        float tip = switch (tier) {
            case LV -> 9.0f / 16.0f;
            case MV -> 10.5f / 16.0f;
            case HV -> 13.0f / 16.0f;
        };

        if (state.hasProperty(WireConnectorBlock.FACING)) {
            Direction facing = state.getValue(WireConnectorBlock.FACING);
            boolean crossarmEnd = state.hasProperty(WireConnectorBlock.CROSSARM_END) && state.getValue(WireConnectorBlock.CROSSARM_END);

            float yCenter = crossarmEnd ? (13.0f / 16.0f) : 0.5f;

            return switch (facing) {
                case UP    -> new Vector3f(0.5f, tip, 0.5f);
                case DOWN  -> new Vector3f(0.5f, 1.0f - tip, 0.5f);
                case NORTH -> new Vector3f(0.5f, yCenter, 1.0f - tip);
                case SOUTH -> new Vector3f(0.5f, yCenter, tip);
                case WEST  -> new Vector3f(1.0f - tip, yCenter, 0.5f);
                case EAST  -> new Vector3f(tip, yCenter, 0.5f);
            };
        }

        if (state.getBlock() instanceof com.powergrid.block.PoleStreetLightBlock) {
            Direction facing = state.getValue(com.powergrid.block.PoleStreetLightBlock.FACING);
            float y = 14.5f / 16.0f;
            return switch (facing) {
                case NORTH -> new Vector3f(0.5f, y, 14.0f / 16.0f);
                case SOUTH -> new Vector3f(0.5f, y, 2.0f / 16.0f);
                case WEST  -> new Vector3f(14.0f / 16.0f, y, 0.5f);
                case EAST  -> new Vector3f(2.0f / 16.0f, y, 0.5f);
                default    -> new Vector3f(0.5f, y, 0.5f);
            };
        }

        if (state.hasProperty(BreakerSwitchBlock.FACING)) {
            Direction facing = state.getValue(BreakerSwitchBlock.FACING);
            float y = 0.65f;
            return switch (facing) {
                case NORTH -> new Vector3f(0.5f, y, 0.75f);
                case SOUTH -> new Vector3f(0.5f, y, 0.25f);
                case WEST  -> new Vector3f(0.75f, y, 0.5f);
                case EAST  -> new Vector3f(0.25f, y, 0.5f);
                default    -> new Vector3f(0.5f, 0.5f, 0.5f);
            };
        }

        return new Vector3f(0.5f, 0.5f, 0.5f);
    }
}

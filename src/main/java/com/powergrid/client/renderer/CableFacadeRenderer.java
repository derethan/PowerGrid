package com.powergrid.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.powergrid.compat.refinery.CableFacadeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Renders 3D Pipe Facades on AWG Power Cables (Solid 2-pixel panels and Pass-Through / Hollow 2-pixel frames)
 * using the target block's own TextureAtlasSprites with full normal and texture-mapping preservation.
 */
@OnlyIn(Dist.CLIENT)
public final class CableFacadeRenderer {

    private static final float THICK = 2.0f / 16.0f; // 0.125f (2 pixels thick)
    private static final float HOLE_MIN = 4.8f / 16.0f; // 0.30f (matches 6.4px facade opening)
    private static final float HOLE_MAX = 11.2f / 16.0f; // 0.70f
    private static final float EPS = 0.0005f;

    private CableFacadeRenderer() {}

    public static void submitFacades(
            Map<Direction, CableFacadeData> facades,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int lightCoords
    ) {
        if (facades == null || facades.isEmpty()) return;

        poseStack.pushPose();
        collector.submitCustomGeometry(
                poseStack,
                RenderTypes.entitySolid(TextureAtlas.LOCATION_BLOCKS),
                (pose, consumer) -> {
                    Matrix4f m = pose.pose();
                    for (Map.Entry<Direction, CableFacadeData> entry : facades.entrySet()) {
                        Direction side = entry.getKey();
                        CableFacadeData data = entry.getValue();
                        if (side != null && data != null && data.block() != null) {
                            renderFacadeOnSide(m, consumer, side, data.block(), data.hollow(), lightCoords, OverlayTexture.NO_OVERLAY);
                        }
                    }
                }
        );
        poseStack.popPose();
    }

    private static TextureAtlasSprite getSpriteForFace(Block block, Direction face) {
        BlockState state = block.defaultBlockState();
        var modelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
        try {
            BlockStateModel model = modelSet.get(state);
            if (model != null) {
                List<BlockStateModelPart> parts = new ArrayList<>();
                model.collectParts(RandomSource.create(42L), parts);
                for (BlockStateModelPart part : parts) {
                    List<BakedQuad> quads = part.getQuads(face);
                    if (quads != null && !quads.isEmpty() && quads.get(0).materialInfo().sprite() != null) {
                        return quads.get(0).materialInfo().sprite();
                    }
                }
                for (BlockStateModelPart part : parts) {
                    List<BakedQuad> unculled = part.getQuads(null);
                    if (unculled != null && !unculled.isEmpty()) {
                        for (BakedQuad quad : unculled) {
                            if (quad.direction() == face && quad.materialInfo().sprite() != null) {
                                return quad.materialInfo().sprite();
                            }
                        }
                        if (unculled.get(0).materialInfo().sprite() != null) {
                            return unculled.get(0).materialInfo().sprite();
                        }
                    }
                }
                if (model.particleMaterial() != null && model.particleMaterial().sprite() != null) {
                    return model.particleMaterial().sprite();
                }
            }
        } catch (Exception ignored) {
        }
        return modelSet.getParticleMaterial(state).sprite();
    }

    private static void renderFacadeOnSide(
            Matrix4f m,
            VertexConsumer c,
            Direction side,
            Block block,
            boolean hollow,
            int light,
            int overlay
    ) {
        TextureAtlasSprite sDown  = getSpriteForFace(block, Direction.DOWN);
        TextureAtlasSprite sUp    = getSpriteForFace(block, Direction.UP);
        TextureAtlasSprite sNorth = getSpriteForFace(block, Direction.NORTH);
        TextureAtlasSprite sSouth = getSpriteForFace(block, Direction.SOUTH);
        TextureAtlasSprite sWest  = getSpriteForFace(block, Direction.WEST);
        TextureAtlasSprite sEast  = getSpriteForFace(block, Direction.EAST);

        if (!hollow) {
            // Solid 16x16x2 panel on the target side
            switch (side) {
                case DOWN  -> renderTexturedBox(m, c, EPS, EPS, EPS, 1.0f - EPS, THICK, 1.0f - EPS, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                case UP    -> renderTexturedBox(m, c, EPS, 1.0f - THICK, EPS, 1.0f - EPS, 1.0f - EPS, 1.0f - EPS, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                case NORTH -> renderTexturedBox(m, c, EPS, EPS, EPS, 1.0f - EPS, 1.0f - EPS, THICK, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                case SOUTH -> renderTexturedBox(m, c, EPS, EPS, 1.0f - THICK, 1.0f - EPS, 1.0f - EPS, 1.0f - EPS, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                case WEST  -> renderTexturedBox(m, c, EPS, EPS, EPS, THICK, 1.0f - EPS, 1.0f - EPS, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                case EAST  -> renderTexturedBox(m, c, 1.0f - THICK, EPS, EPS, 1.0f - EPS, 1.0f - EPS, 1.0f - EPS, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
            }
        } else {
            // Pass-Through / Hollow frame: 4 rectangular strips surrounding the central [HOLE_MIN, HOLE_MAX] opening
            switch (side) {
                case DOWN -> {
                    float y0 = EPS, y1 = THICK;
                    renderTexturedBox(m, c, EPS, y0, EPS, 1.0f - EPS, y1, HOLE_MIN, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, EPS, y0, HOLE_MAX, 1.0f - EPS, y1, 1.0f - EPS, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, EPS, y0, HOLE_MIN, HOLE_MIN, y1, HOLE_MAX, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, HOLE_MAX, y0, HOLE_MIN, 1.0f - EPS, y1, HOLE_MAX, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                }
                case UP -> {
                    float y0 = 1.0f - THICK, y1 = 1.0f - EPS;
                    renderTexturedBox(m, c, EPS, y0, EPS, 1.0f - EPS, y1, HOLE_MIN, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, EPS, y0, HOLE_MAX, 1.0f - EPS, y1, 1.0f - EPS, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, EPS, y0, HOLE_MIN, HOLE_MIN, y1, HOLE_MAX, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, HOLE_MAX, y0, HOLE_MIN, 1.0f - EPS, y1, HOLE_MAX, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                }
                case NORTH -> {
                    float z0 = EPS, z1 = THICK;
                    renderTexturedBox(m, c, EPS, EPS, z0, 1.0f - EPS, HOLE_MIN, z1, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, EPS, HOLE_MAX, z0, 1.0f - EPS, 1.0f - EPS, z1, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, EPS, HOLE_MIN, z0, HOLE_MIN, HOLE_MAX, z1, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, HOLE_MAX, HOLE_MIN, z0, 1.0f - EPS, HOLE_MAX, z1, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                }
                case SOUTH -> {
                    float z0 = 1.0f - THICK, z1 = 1.0f - EPS;
                    renderTexturedBox(m, c, EPS, EPS, z0, 1.0f - EPS, HOLE_MIN, z1, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, EPS, HOLE_MAX, z0, 1.0f - EPS, 1.0f - EPS, z1, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, EPS, HOLE_MIN, z0, HOLE_MIN, HOLE_MAX, z1, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, HOLE_MAX, HOLE_MIN, z0, 1.0f - EPS, HOLE_MAX, z1, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                }
                case WEST -> {
                    float x0 = EPS, x1 = THICK;
                    renderTexturedBox(m, c, x0, EPS, EPS, x1, HOLE_MIN, 1.0f - EPS, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, x0, HOLE_MAX, EPS, x1, 1.0f - EPS, 1.0f - EPS, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, x0, HOLE_MIN, EPS, x1, HOLE_MAX, HOLE_MIN, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, x0, HOLE_MIN, HOLE_MAX, x1, HOLE_MAX, 1.0f - EPS, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                }
                case EAST -> {
                    float x0 = 1.0f - THICK, x1 = 1.0f - EPS;
                    renderTexturedBox(m, c, x0, EPS, EPS, x1, HOLE_MIN, 1.0f - EPS, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, x0, HOLE_MAX, EPS, x1, 1.0f - EPS, 1.0f - EPS, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, x0, HOLE_MIN, EPS, x1, HOLE_MAX, HOLE_MIN, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                    renderTexturedBox(m, c, x0, HOLE_MIN, HOLE_MAX, x1, HOLE_MAX, 1.0f - EPS, sDown, sUp, sNorth, sSouth, sWest, sEast, light, overlay);
                }
            }
        }
    }

    private static void renderTexturedBox(
            Matrix4f m,
            VertexConsumer c,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            TextureAtlasSprite sDown,
            TextureAtlasSprite sUp,
            TextureAtlasSprite sNorth,
            TextureAtlasSprite sSouth,
            TextureAtlasSprite sWest,
            TextureAtlasSprite sEast,
            int light,
            int overlay
    ) {
        // DOWN (-Y)
        {
            float u0 = sDown.getU(x0), u1 = sDown.getU(x1);
            float v0 = sDown.getV(1.0f - z1), v1 = sDown.getV(1.0f - z0);
            c.addVertex(m, x0, y0, z1).setColor(1f, 1f, 1f, 1f).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0f, -1f, 0f);
            c.addVertex(m, x0, y0, z0).setColor(1f, 1f, 1f, 1f).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0f, -1f, 0f);
            c.addVertex(m, x1, y0, z0).setColor(1f, 1f, 1f, 1f).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0f, -1f, 0f);
            c.addVertex(m, x1, y0, z1).setColor(1f, 1f, 1f, 1f).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0f, -1f, 0f);
        }
        // UP (+Y)
        {
            float u0 = sUp.getU(x0), u1 = sUp.getU(x1);
            float v0 = sUp.getV(z0), v1 = sUp.getV(z1);
            c.addVertex(m, x0, y1, z0).setColor(1f, 1f, 1f, 1f).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0f, 1f, 0f);
            c.addVertex(m, x0, y1, z1).setColor(1f, 1f, 1f, 1f).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0f, 1f, 0f);
            c.addVertex(m, x1, y1, z1).setColor(1f, 1f, 1f, 1f).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0f, 1f, 0f);
            c.addVertex(m, x1, y1, z0).setColor(1f, 1f, 1f, 1f).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0f, 1f, 0f);
        }
        // NORTH (-Z)
        {
            float u0 = sNorth.getU(1.0f - x1), u1 = sNorth.getU(1.0f - x0);
            float v0 = sNorth.getV(1.0f - y1), v1 = sNorth.getV(1.0f - y0);
            c.addVertex(m, x1, y1, z0).setColor(1f, 1f, 1f, 1f).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0f, 0f, -1f);
            c.addVertex(m, x1, y0, z0).setColor(1f, 1f, 1f, 1f).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0f, 0f, -1f);
            c.addVertex(m, x0, y0, z0).setColor(1f, 1f, 1f, 1f).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0f, 0f, -1f);
            c.addVertex(m, x0, y1, z0).setColor(1f, 1f, 1f, 1f).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0f, 0f, -1f);
        }
        // SOUTH (+Z)
        {
            float u0 = sSouth.getU(x0), u1 = sSouth.getU(x1);
            float v0 = sSouth.getV(1.0f - y1), v1 = sSouth.getV(1.0f - y0);
            c.addVertex(m, x0, y1, z1).setColor(1f, 1f, 1f, 1f).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0f, 0f, 1f);
            c.addVertex(m, x0, y0, z1).setColor(1f, 1f, 1f, 1f).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0f, 0f, 1f);
            c.addVertex(m, x1, y0, z1).setColor(1f, 1f, 1f, 1f).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0f, 0f, 1f);
            c.addVertex(m, x1, y1, z1).setColor(1f, 1f, 1f, 1f).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0f, 0f, 1f);
        }
        // WEST (-X)
        {
            float u0 = sWest.getU(z0), u1 = sWest.getU(z1);
            float v0 = sWest.getV(1.0f - y1), v1 = sWest.getV(1.0f - y0);
            c.addVertex(m, x0, y1, z0).setColor(1f, 1f, 1f, 1f).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(-1f, 0f, 0f);
            c.addVertex(m, x0, y0, z0).setColor(1f, 1f, 1f, 1f).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(-1f, 0f, 0f);
            c.addVertex(m, x0, y0, z1).setColor(1f, 1f, 1f, 1f).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(-1f, 0f, 0f);
            c.addVertex(m, x0, y1, z1).setColor(1f, 1f, 1f, 1f).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(-1f, 0f, 0f);
        }
        // EAST (+X)
        {
            float u0 = sEast.getU(1.0f - z1), u1 = sEast.getU(1.0f - z0);
            float v0 = sEast.getV(1.0f - y1), v1 = sEast.getV(1.0f - y0);
            c.addVertex(m, x1, y1, z1).setColor(1f, 1f, 1f, 1f).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(1f, 0f, 0f);
            c.addVertex(m, x1, y0, z1).setColor(1f, 1f, 1f, 1f).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(1f, 0f, 0f);
            c.addVertex(m, x1, y0, z0).setColor(1f, 1f, 1f, 1f).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(1f, 0f, 0f);
            c.addVertex(m, x1, y1, z0).setColor(1f, 1f, 1f, 1f).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(1f, 0f, 0f);
        }
    }
}

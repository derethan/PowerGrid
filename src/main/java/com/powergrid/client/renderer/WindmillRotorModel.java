package com.powergrid.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class WindmillRotorModel {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("powergrid", "textures/entity/windmill_rotor.png");
    private static final float TEX_W = 256.0f;
    private static final float TEX_H = 256.0f;

    public record UVRect(float u0, float v0, float u1, float v1) {
        public static UVRect of(int x, int y, int w, int h) {
            return new UVRect(
                    (x + 0.5f) / TEX_W,
                    (y + 0.5f) / TEX_H,
                    (x + w - 0.5f) / TEX_W,
                    (y + h - 0.5f) / TEX_H
            );
        }
    }

    public record Quad(
            float xA, float yA, float zA,
            float xB, float yB, float zB,
            float xC, float yC, float zC,
            float xD, float yD, float zD,
            UVRect uv,
            float nx, float ny, float nz
    ) {}

    // Atlas UV Mapping Regions
    private static final UVRect UV_WOOD_SAIL      = UVRect.of(0, 0, 128, 64);
    private static final UVRect UV_WOOD_SPAR      = UVRect.of(128, 0, 64, 64);
    private static final UVRect UV_IRON_HUB       = UVRect.of(192, 0, 64, 64);
    private static final UVRect UV_AXLE_SHAFT     = UVRect.of(0, 64, 64, 64);
    private static final UVRect UV_STEEL_BRACKET  = UVRect.of(64, 64, 64, 64);
    private static final UVRect UV_BRASS_BEARING  = UVRect.of(128, 64, 64, 32);
    private static final UVRect UV_CANVAS_DETAIL  = UVRect.of(192, 64, 64, 64);

    private final List<Quad> rotorQuads = new ArrayList<>();

    public WindmillRotorModel() {
        buildModel();
    }

    private void buildModel() {
        // 1. Central Axle Timber Shaft (spanning full 16 units across Z axis, 6x6 cross section)
        addBox(-3.0f, -3.0f, -8.0f, 3.0f, 3.0f, 8.0f,
                UV_STEEL_BRACKET, UV_STEEL_BRACKET, UV_AXLE_SHAFT, UV_AXLE_SHAFT, UV_AXLE_SHAFT, UV_AXLE_SHAFT);

        // 2. Heavy Cast Iron Hub Flanges (Front and Back plates)
        addBox(-5.5f, -5.5f, -4.5f, 5.5f, 5.5f, -3.5f,
                UV_IRON_HUB, UV_IRON_HUB, UV_STEEL_BRACKET, UV_STEEL_BRACKET, UV_STEEL_BRACKET, UV_STEEL_BRACKET);
        addBox(-5.5f, -5.5f, 3.5f, 5.5f, 5.5f, 4.5f,
                UV_IRON_HUB, UV_IRON_HUB, UV_STEEL_BRACKET, UV_STEEL_BRACKET, UV_STEEL_BRACKET, UV_STEEL_BRACKET);

        // 3. Central Brass Bearing Nose Dome
        addBox(-2.5f, -2.5f, -6.5f, 2.5f, 2.5f, -4.5f,
                UV_BRASS_BEARING, UV_BRASS_BEARING, UV_BRASS_BEARING, UV_BRASS_BEARING, UV_BRASS_BEARING, UV_BRASS_BEARING);

        // 4. Four Primary Radial Timber Spars & Angled Aerodynamic Sails (0, 90, 180, 270 deg)
        for (int i = 0; i < 4; i++) {
            double angle = i * Math.PI / 2.0;

            // Heavy structural wooden spar beam (from r=4.5 to r=22.8)
            addRadialBeam(4.5f, 22.8f, 2.2f, -1.8f, 1.8f, angle, UV_WOOD_SPAR, UV_STEEL_BRACKET);

            // Steel blade mounting collar at base
            addRadialBeam(4.0f, 7.0f, 3.2f, -2.2f, 2.2f, angle, UV_STEEL_BRACKET, UV_IRON_HUB);

            // Angled Aerodynamic Canvas/Wood Sail Slats attached to the spar
            addAngledSail(angle, UV_WOOD_SAIL, UV_CANVAS_DETAIL);
        }

        // 5. Four Diagonal Structural Bracing Struts (45, 135, 225, 315 deg)
        for (int i = 0; i < 4; i++) {
            double angle = (i * Math.PI / 2.0) + (Math.PI / 4.0);
            addRadialBeam(4.0f, 11.5f, 1.8f, -1.2f, 1.2f, angle, UV_WOOD_SPAR, UV_STEEL_BRACKET);
        }
    }

    private void addBox(float x0, float y0, float z0, float x1, float y1, float z1,
                        UVRect north, UVRect south, UVRect west, UVRect east, UVRect up, UVRect down) {
        // North (z0)
        rotorQuads.add(new Quad(x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0, north, 0, 0, -1));
        // South (z1)
        rotorQuads.add(new Quad(x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1, south, 0, 0, 1));
        // West (x0)
        rotorQuads.add(new Quad(x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1, west, -1, 0, 0));
        // East (x1)
        rotorQuads.add(new Quad(x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0, east, 1, 0, 0));
        // Up (y1)
        rotorQuads.add(new Quad(x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, up, 0, 1, 0));
        // Down (y0)
        rotorQuads.add(new Quad(x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, down, 0, -1, 0));
    }

    private void addRadialBeam(float innerR, float outerR, float beamWidth, float z0, float z1, double angle, UVRect uvSide, UVRect uvEnd) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float perpX = -sin * (beamWidth / 2.0f);
        float perpY = cos * (beamWidth / 2.0f);

        float xA = cos * innerR - perpX;
        float yA = sin * innerR - perpY;
        float xB = cos * innerR + perpX;
        float yB = sin * innerR + perpY;
        float xC = cos * outerR + perpX;
        float yC = sin * outerR + perpY;
        float xD = cos * outerR - perpX;
        float yD = sin * outerR - perpY;

        // Front face (z0)
        rotorQuads.add(new Quad(xC, yC, z0, xB, yB, z0, xA, yA, z0, xD, yD, z0, uvSide, 0, 0, -1));
        // Back face (z1)
        rotorQuads.add(new Quad(xD, yD, z1, xA, yA, z1, xB, yB, z1, xC, yC, z1, uvSide, 0, 0, 1));
        // Side 1
        rotorQuads.add(new Quad(xA, yA, z0, xB, yB, z0, xB, yB, z1, xA, yA, z1, uvSide, -sin, cos, 0));
        // Side 2
        rotorQuads.add(new Quad(xC, yC, z0, xD, yD, z0, xD, yD, z1, xC, yC, z1, uvSide, sin, -cos, 0));
        // Outer Tip
        rotorQuads.add(new Quad(xD, yD, z0, xC, yC, z0, xC, yC, z1, xD, yD, z1, uvEnd, cos, sin, 0));
    }

    private void addAngledSail(double angle, UVRect uvSail, UVRect uvDetail) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        // Perpendicular vector along the rotation plane
        float perpX = -sin;
        float perpY = cos;

        // Sail extends laterally from the spar (width 6.2 units) and along radius from r=7.0 to r=22.4
        float rStart = 7.0f;
        float rEnd = 22.4f;
        float lateralWidth = 6.2f;

        // Pitch angle offset along Z to form authentic aerodynamic aerofoil
        float pitchZ = 2.4f;

        // 4 corners of the sail blade
        float x0 = cos * rStart;
        float y0 = sin * rStart;
        float z0 = -0.6f;

        float x1 = cos * rEnd;
        float y1 = sin * rEnd;
        float z1 = -0.6f;

        float x2 = x1 + perpX * lateralWidth;
        float y2 = y1 + perpY * lateralWidth;
        float z2 = z1 + pitchZ;

        float x3 = x0 + perpX * lateralWidth;
        float y3 = y0 + perpY * lateralWidth;
        float z3 = z0 + pitchZ;

        // Front sail surface (windward)
        rotorQuads.add(new Quad(x0, y0, z0, x1, y1, z1, x2, y2, z2, x3, y3, z3, uvSail, 0.2f, 0.2f, -0.95f));
        // Back sail surface (leeward)
        rotorQuads.add(new Quad(x3, y3, z3, x2, y2, z2, x1, y1, z1, x0, y0, z0, uvDetail, -0.2f, -0.2f, 0.95f));

        // Lateral sail cross-rib battens (3 battens across the sail)
        for (int b = 1; b <= 3; b++) {
            float frac = b / 4.0f;
            float rb = rStart + (rEnd - rStart) * frac;
            float bx0 = cos * rb;
            float by0 = sin * rb;
            float bx1 = bx0 + perpX * lateralWidth;
            float by1 = by0 + perpY * lateralWidth;
            float bz0 = z0;
            float bz1 = z0 + pitchZ;

            rotorQuads.add(new Quad(bx0 - perpX * 0.4f, by0 - perpY * 0.4f, bz0,
                    bx1 - perpX * 0.4f, by1 - perpY * 0.4f, bz1,
                    bx1 + perpX * 0.4f, by1 + perpY * 0.4f, bz1,
                    bx0 + perpX * 0.4f, by0 + perpY * 0.4f, bz0, uvDetail, 0, 0, -1));
        }
    }

    public void render(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light) {
        submitNodeCollector.submitCustomGeometry(
                poseStack,
                RenderTypes.entitySolid(TEXTURE),
                (pose, consumer) -> {
                    Matrix4f m = pose.pose();
                    for (Quad q : this.rotorQuads) {
                        float xA = q.xA() / 16.0f, yA = q.yA() / 16.0f, zA = q.zA() / 16.0f;
                        float xB = q.xB() / 16.0f, yB = q.yB() / 16.0f, zB = q.zB() / 16.0f;
                        float xC = q.xC() / 16.0f, yC = q.yC() / 16.0f, zC = q.zC() / 16.0f;
                        float xD = q.xD() / 16.0f, yD = q.yD() / 16.0f, zD = q.zD() / 16.0f;

                        emitVertex(consumer, pose, m, xA, yA, zA, q.uv().u0(), q.uv().v0(), q.nx(), q.ny(), q.nz(), light);
                        emitVertex(consumer, pose, m, xB, yB, zB, q.uv().u0(), q.uv().v1(), q.nx(), q.ny(), q.nz(), light);
                        emitVertex(consumer, pose, m, xC, yC, zC, q.uv().u1(), q.uv().v1(), q.nx(), q.ny(), q.nz(), light);
                        emitVertex(consumer, pose, m, xD, yD, zD, q.uv().u1(), q.uv().v0(), q.nx(), q.ny(), q.nz(), light);
                    }
                }
        );
    }

    private static void emitVertex(VertexConsumer consumer, PoseStack.Pose pose, Matrix4f m,
                                   float x, float y, float z, float u, float v,
                                   float nx, float ny, float nz, int light) {
        consumer.addVertex(m, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}

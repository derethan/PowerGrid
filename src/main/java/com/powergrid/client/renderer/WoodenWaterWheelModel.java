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

public class WoodenWaterWheelModel {
    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("powergrid", "textures/entity/wooden_water_wheel.png");
    private static final float TEX_W = 256.0f;
    private static final float TEX_H = 256.0f;

    public record UVRect(float u0, float v0, float u1, float v1) {
        public static UVRect of(int x, int y, int w, int h) {
            // Half-texel (0.5px) inset margin prevents mipmap / bilinear filtering texture bleed
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
    private static final UVRect UV_WOOD_PLANK  = UVRect.of(0, 0, 64, 64);
    private static final UVRect UV_WOOD_END    = UVRect.of(64, 0, 32, 32);
    private static final UVRect UV_IRON_HUB    = UVRect.of(96, 0, 64, 64);
    private static final UVRect UV_IRON_STRAP  = UVRect.of(160, 0, 64, 32);
    private static final UVRect UV_PADDLE      = UVRect.of(0, 64, 64, 48);
    private static final UVRect UV_AXLE_CAP    = UVRect.of(64, 64, 32, 32);
    private static final UVRect UV_AXLE_WOOD   = UVRect.of(96, 64, 64, 32);
    private static final UVRect UV_RIM_FELLOE  = UVRect.of(0, 112, 64, 32);
    private static final UVRect UV_SPOKE       = UVRect.of(64, 112, 64, 16);

    private final List<Quad> wheelQuads = new ArrayList<>();

    public WoodenWaterWheelModel() {
        buildModel();
    }

    private void buildModel() {
        // 1. Central Axle Timber Shaft (spanning full 16 units across Z axis)
        addBox(-3.0f, -3.0f, -8.0f, 3.0f, 3.0f, 8.0f,
                UV_AXLE_CAP, UV_AXLE_CAP, UV_AXLE_WOOD, UV_AXLE_WOOD, UV_AXLE_WOOD, UV_AXLE_WOOD);

        // 2. Heavy Cast Iron Hub Flanges (Front and Back)
        addBox(-6.0f, -6.0f, -5.5f, 6.0f, 6.0f, -4.5f,
                UV_IRON_HUB, UV_IRON_HUB, UV_IRON_STRAP, UV_IRON_STRAP, UV_IRON_STRAP, UV_IRON_STRAP);
        addBox(-6.0f, -6.0f, 4.5f, 6.0f, 6.0f, 5.5f,
                UV_IRON_HUB, UV_IRON_HUB, UV_IRON_STRAP, UV_IRON_STRAP, UV_IRON_STRAP, UV_IRON_STRAP);

        // 3. 8 Radial Timber Spokes (from r=5.0 to r=20.5)
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0;
            addRadialBeam(5.0f, 20.5f, 2.5f, -2.0f, 2.0f, angle, UV_SPOKE, UV_WOOD_END);
        }

        // 4. Outer Polygonal Rim Rings (12 segments around diameter r=20.5, front and back)
        int segments = 12;
        float rimInnerR = 19.5f;
        float rimOuterR = 21.8f;

        for (int i = 0; i < segments; i++) {
            double a0 = i * 2.0 * Math.PI / segments;
            double a1 = (i + 1) * 2.0 * Math.PI / segments;

            // Front rim segment (z: -5.0 to -3.2)
            addRimSegment(rimInnerR, rimOuterR, -5.0f, -3.2f, a0, a1, UV_RIM_FELLOE, UV_IRON_STRAP);
            // Back rim segment (z: 3.2 to 5.0)
            addRimSegment(rimInnerR, rimOuterR, 3.2f, 5.0f, a0, a1, UV_RIM_FELLOE, UV_IRON_STRAP);
        }

        // 5. 12 Water Buckets / Paddles (between rim rings, from r=17.5 out to r=23.5)
        for (int i = 0; i < segments; i++) {
            double angle = i * 2.0 * Math.PI / segments;
            addPaddleBucket(17.5f, 23.5f, -4.0f, 4.0f, angle, UV_PADDLE);
        }
    }

    private void addBox(float x0, float y0, float z0, float x1, float y1, float z1,
                        UVRect north, UVRect south, UVRect west, UVRect east, UVRect up, UVRect down) {
        // North (z0)
        wheelQuads.add(new Quad(x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0, north, 0, 0, -1));
        // South (z1)
        wheelQuads.add(new Quad(x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1, south, 0, 0, 1));
        // West (x0)
        wheelQuads.add(new Quad(x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1, west, -1, 0, 0));
        // East (x1)
        wheelQuads.add(new Quad(x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0, east, 1, 0, 0));
        // Up (y1)
        wheelQuads.add(new Quad(x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, up, 0, 1, 0));
        // Down (y0)
        wheelQuads.add(new Quad(x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, down, 0, -1, 0));
    }

    private void addRadialBeam(float r0, float r1, float halfWidth, float z0, float z1, double angle, UVRect sideUV, UVRect capUV) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double nCos = -sin; // perpendicular normal vector
        double nSin = cos;

        float xA0 = (float) (r0 * cos - halfWidth * nCos);
        float yA0 = (float) (r0 * sin - halfWidth * nSin);
        float xB0 = (float) (r0 * cos + halfWidth * nCos);
        float yB0 = (float) (r0 * sin + halfWidth * nSin);

        float xA1 = (float) (r1 * cos - halfWidth * nCos);
        float yA1 = (float) (r1 * sin - halfWidth * nSin);
        float xB1 = (float) (r1 * cos + halfWidth * nCos);
        float yB1 = (float) (r1 * sin + halfWidth * nSin);

        // Front Face (z = z0)
        wheelQuads.add(new Quad(xB1, yB1, z0, xB0, yB0, z0, xA0, yA0, z0, xA1, yA1, z0, sideUV, 0, 0, -1));
        // Back Face (z = z1)
        wheelQuads.add(new Quad(xA1, yA1, z1, xA0, yA0, z1, xB0, yB0, z1, xB1, yB1, z1, sideUV, 0, 0, 1));

        // Side A
        wheelQuads.add(new Quad(xA1, yA1, z0, xA0, yA0, z0, xA0, yA0, z1, xA1, yA1, z1, sideUV, (float) -nCos, (float) -nSin, 0));
        // Side B
        wheelQuads.add(new Quad(xB0, yB0, z0, xB1, yB1, z0, xB1, yB1, z1, xB0, yB0, z1, sideUV, (float) nCos, (float) nSin, 0));

        // Outer Cap
        wheelQuads.add(new Quad(xA1, yA1, z0, xA1, yA1, z1, xB1, yB1, z1, xB1, yB1, z0, capUV, (float) cos, (float) sin, 0));
    }

    private void addRimSegment(float rIn, float rOut, float z0, float z1, double a0, double a1, UVRect faceUV, UVRect edgeUV) {
        float xIn0 = (float) (rIn * Math.cos(a0)),  yIn0 = (float) (rIn * Math.sin(a0));
        float xOut0 = (float) (rOut * Math.cos(a0)), yOut0 = (float) (rOut * Math.sin(a0));
        float xIn1 = (float) (rIn * Math.cos(a1)),  yIn1 = (float) (rIn * Math.sin(a1));
        float xOut1 = (float) (rOut * Math.cos(a1)), yOut1 = (float) (rOut * Math.sin(a1));

        // Front face (z = z0)
        wheelQuads.add(new Quad(xOut1, yOut1, z0, xOut0, yOut0, z0, xIn0, yIn0, z0, xIn1, yIn1, z0, faceUV, 0, 0, -1));
        // Back face (z = z1)
        wheelQuads.add(new Quad(xIn1, yIn1, z1, xIn0, yIn0, z1, xOut0, yOut0, z1, xOut1, yOut1, z1, faceUV, 0, 0, 1));

        // Outer rim curved plate
        double midA = (a0 + a1) * 0.5;
        float nx = (float) Math.cos(midA);
        float ny = (float) Math.sin(midA);
        wheelQuads.add(new Quad(xOut0, yOut0, z0, xOut1, yOut1, z0, xOut1, yOut1, z1, xOut0, yOut0, z1, edgeUV, nx, ny, 0));

        // Inner rim plate
        wheelQuads.add(new Quad(xIn1, yIn1, z0, xIn0, yIn0, z0, xIn0, yIn0, z1, xIn1, yIn1, z1, edgeUV, -nx, -ny, 0));
    }

    private void addPaddleBucket(float rIn, float rOut, float z0, float z1, double angle, UVRect paddleUV) {
        // Scoop angle slightly tilted (~15 degrees) to catch falling water
        double scoopA = angle - Math.toRadians(15.0);
        double cos = Math.cos(scoopA);
        double sin = Math.sin(scoopA);

        float xIn = (float) (rIn * Math.cos(angle));
        float yIn = (float) (rIn * Math.sin(angle));
        float xOut = (float) (xIn + (rOut - rIn) * cos);
        float yOut = (float) (yIn + (rOut - rIn) * sin);

        float nx = (float) -sin;
        float ny = (float) cos;

        // Front face of paddle bucket
        wheelQuads.add(new Quad(xOut, yOut, z0, xIn, yIn, z0, xIn, yIn, z1, xOut, yOut, z1, paddleUV, nx, ny, 0));
        // Back face of paddle bucket
        wheelQuads.add(new Quad(xIn, yIn, z0, xOut, yOut, z0, xOut, yOut, z1, xIn, yIn, z1, paddleUV, -nx, -ny, 0));
    }

    public void render(PoseStack poseStack, SubmitNodeCollector collector, int light) {
        collector.submitCustomGeometry(poseStack, RenderTypes.entitySolid(TEXTURE), (pose, consumer) -> {
            Matrix4f mat = pose.pose();
            for (Quad q : this.wheelQuads) {
                emitQuad(pose, mat, consumer, q, light);
            }
        });
    }

    private static void emitQuad(PoseStack.Pose pose, Matrix4f mat, VertexConsumer vc, Quad q, int light) {
        // Convert from 16-pixel grid coordinates to Minecraft 1.0 unit world space
        float s = 1.0f / 16.0f;
        float xA = q.xA * s, yA = q.yA * s, zA = q.zA * s;
        float xB = q.xB * s, yB = q.yB * s, zB = q.zB * s;
        float xC = q.xC * s, yC = q.yC * s, zC = q.zC * s;
        float xD = q.xD * s, yD = q.yD * s, zD = q.zD * s;

        vc.addVertex(mat, xA, yA, zA).setColor(255, 255, 255, 255).setUv(q.uv.u0(), q.uv.v0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, q.nx, q.ny, q.nz);
        vc.addVertex(mat, xB, yB, zB).setColor(255, 255, 255, 255).setUv(q.uv.u0(), q.uv.v1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, q.nx, q.ny, q.nz);
        vc.addVertex(mat, xC, yC, zC).setColor(255, 255, 255, 255).setUv(q.uv.u1(), q.uv.v1()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, q.nx, q.ny, q.nz);
        vc.addVertex(mat, xD, yD, zD).setColor(255, 255, 255, 255).setUv(q.uv.u1(), q.uv.v0()).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, q.nx, q.ny, q.nz);
    }
}

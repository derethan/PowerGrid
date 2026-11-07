package com.powergrid.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class SolarPanelModel {
    private static final float TEX_W = 256.0f;
    private static final float TEX_H = 256.0f;

    public record UVRect(float u0, float v0, float u1, float v1) {
        public static UVRect of(float x, float y, float w, float h) {
            return new UVRect(
                    (x + 0.15f) / TEX_W,
                    (y + 0.15f) / TEX_H,
                    (x + w - 0.15f) / TEX_W,
                    (y + h - 0.15f) / TEX_H
            );
        }
    }

    // 256x256 Atlas UV Regions
    public static final UVRect UV_ALUMINUM_FRAME   = UVRect.of(128, 0, 64, 64);
    public static final UVRect UV_INVERTER_HOUSING = UVRect.of(192, 0, 64, 64);
    public static final UVRect UV_MOUNTING_STRUT   = UVRect.of(128, 64, 64, 64);
    public static final UVRect UV_CORNER_BRACKET   = UVRect.of(192, 64, 64, 64);
    public static final UVRect UV_SILVER_BUSBAR    = UVRect.of(128, 128, 64, 32);
    public static final UVRect UV_LED_ACTIVE       = UVRect.of(192, 128, 32, 32);
    public static final UVRect UV_LED_IDLE         = UVRect.of(224, 128, 32, 32);
    public static final UVRect UV_RUBBER_FOOT      = UVRect.of(128, 160, 64, 32);

    private static final int FULL_BRIGHT_LIGHT = 15728880;

    public void render(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            SolarPanelRenderState state,
            int light,
            int overlay
    ) {
        Matrix4f m = pose.pose();

        boolean n = state.north;
        boolean s = state.south;
        boolean e = state.east;
        boolean w = state.west;

        // 1. PHOTOVOLTAIC WAFER DECK (Expands seamlessly to block edges when connected)
        float wX0 = w ? -8.0f : -6.8f;
        float wX1 = e ?  8.0f :  6.8f;
        float wZ0 = n ? -8.0f : -6.8f;
        float wZ1 = s ?  8.0f :  6.8f;

        // Proportional UV mapping into the 128x128 Solar Wafer quadrant (0, 0, 128, 128)
        float uWafer0 = (wX0 + 8.0f) * 8.0f;
        float uWafer1 = (wX1 + 8.0f) * 8.0f;
        float vWafer0 = (wZ0 + 8.0f) * 8.0f;
        float vWafer1 = (wZ1 + 8.0f) * 8.0f;
        UVRect waferTopUV = UVRect.of(uWafer0, vWafer0, uWafer1 - uWafer0, vWafer1 - vWafer0);

        emitBox(pose, m, consumer, wX0, 4.0f, wZ0, wX1, 6.4f, wZ1,
                UV_INVERTER_HOUSING, UV_INVERTER_HOUSING, UV_INVERTER_HOUSING, UV_INVERTER_HOUSING,
                waferTopUV, UV_INVERTER_HOUSING, light, overlay);

        // 2. 3D RAISED COLLECTOR BUSBARS & SEAM INTERCONNECTS (Y: 6.4 .. 6.52)
        emitBoxUniform(pose, m, consumer, -4.15f, 6.4f, wZ0, -3.85f, 6.50f, wZ1, UV_SILVER_BUSBAR, light, overlay);
        emitBoxUniform(pose, m, consumer,  3.85f, 6.4f, wZ0,  4.15f, 6.50f, wZ1, UV_SILVER_BUSBAR, light, overlay);
        emitBoxUniform(pose, m, consumer, wX0, 6.4f, -4.15f, wX1, 6.50f, -3.85f, UV_SILVER_BUSBAR, light, overlay);
        emitBoxUniform(pose, m, consumer, wX0, 6.4f,  3.85f, wX1, 6.50f,  4.15f, UV_SILVER_BUSBAR, light, overlay);

        // Seamless inter-panel busbar bridge strips along connected seams
        if (n) {
            emitBoxUniform(pose, m, consumer, wX0, 6.4f, -8.0f, wX1, 6.52f, -7.82f, UV_SILVER_BUSBAR, light, overlay);
        }
        if (s) {
            emitBoxUniform(pose, m, consumer, wX0, 6.4f,  7.82f, wX1, 6.52f,  8.0f, UV_SILVER_BUSBAR, light, overlay);
        }
        if (w) {
            emitBoxUniform(pose, m, consumer, -8.0f, 6.4f, wZ0, -7.82f, 6.52f, wZ1, UV_SILVER_BUSBAR, light, overlay);
        }
        if (e) {
            emitBoxUniform(pose, m, consumer,  7.82f, 6.4f, wZ0,  8.0f, 6.52f, wZ1, UV_SILVER_BUSBAR, light, overlay);
        }

        // 3. ADAPTIVE OUTER FRAME BEZEL
        UVRect ledUV = state.active ? UV_LED_ACTIVE : UV_LED_IDLE;
        int ledLight = state.active ? FULL_BRIGHT_LIGHT : light;

        // North Outer Bezel
        if (!n) {
            float bx0 = w ? -8.0f : -6.6f;
            float bx1 = e ?  8.0f :  6.6f;
            emitBoxUniform(pose, m, consumer, bx0, 3.8f, -8.0f, bx1, 7.0f, -6.8f, UV_ALUMINUM_FRAME, light, overlay);
            emitBoxUniform(pose, m, consumer, bx0, 6.4f, -6.8f, bx1, 6.75f, -6.45f, UV_ALUMINUM_FRAME, light, overlay);
            emitBoxUniform(pose, m, consumer, -2.0f, 4.9f, -8.05f, 2.0f, 5.7f, -7.95f, ledUV, ledLight, overlay);
        }

        // South Outer Bezel
        if (!s) {
            float bx0 = w ? -8.0f : -6.6f;
            float bx1 = e ?  8.0f :  6.6f;
            emitBoxUniform(pose, m, consumer, bx0, 3.8f, 6.8f, bx1, 7.0f, 8.0f, UV_ALUMINUM_FRAME, light, overlay);
            emitBoxUniform(pose, m, consumer, bx0, 6.4f, 6.45f, bx1, 6.75f, 6.8f, UV_ALUMINUM_FRAME, light, overlay);
            emitBoxUniform(pose, m, consumer, -2.0f, 4.9f, 7.95f, 2.0f, 5.7f, 8.05f, ledUV, ledLight, overlay);
        }

        // West Outer Bezel
        if (!w) {
            float bz0 = n ? -8.0f : -6.6f;
            float bz1 = s ?  8.0f :  6.6f;
            emitBoxUniform(pose, m, consumer, -8.0f, 3.8f, bz0, -6.8f, 7.0f, bz1, UV_ALUMINUM_FRAME, light, overlay);
            emitBoxUniform(pose, m, consumer, -6.8f, 6.4f, bz0, -6.45f, 6.75f, bz1, UV_ALUMINUM_FRAME, light, overlay);
            emitBoxUniform(pose, m, consumer, -8.05f, 4.9f, -2.0f, -7.95f, 5.7f, 2.0f, ledUV, ledLight, overlay);
        }

        // East Outer Bezel
        if (!e) {
            float bz0 = n ? -8.0f : -6.6f;
            float bz1 = s ?  8.0f :  6.6f;
            emitBoxUniform(pose, m, consumer, 6.8f, 3.8f, bz0, 8.0f, 7.0f, bz1, UV_ALUMINUM_FRAME, light, overlay);
            emitBoxUniform(pose, m, consumer, 6.45f, 6.4f, bz0, 6.8f, 6.75f, bz1, UV_ALUMINUM_FRAME, light, overlay);
            emitBoxUniform(pose, m, consumer, 7.95f, 4.9f, -2.0f, 8.05f, 5.7f, 2.0f, ledUV, ledLight, overlay);
        }

        // 4. CORNER BRACKETS & L-JUNCTION CAPS
        // North-West Corner
        if (!n && !w) {
            emitBoxUniform(pose, m, consumer, -8.0f, 3.6f, -8.0f, -6.6f, 7.15f, -6.6f, UV_CORNER_BRACKET, light, overlay);
            renderCornerFoot(pose, m, consumer, -7.8f, -7.8f, light, overlay);
        } else if (n && w && !state.northWest) {
            emitBoxUniform(pose, m, consumer, -8.0f, 3.8f, -8.0f, -6.8f, 7.0f, -6.8f, UV_CORNER_BRACKET, light, overlay);
        }

        // North-East Corner
        if (!n && !e) {
            emitBoxUniform(pose, m, consumer, 6.6f, 3.6f, -8.0f, 8.0f, 7.15f, -6.6f, UV_CORNER_BRACKET, light, overlay);
            renderCornerFoot(pose, m, consumer, 5.8f, -7.8f, light, overlay);
        } else if (n && e && !state.northEast) {
            emitBoxUniform(pose, m, consumer, 6.8f, 3.8f, -8.0f, 8.0f, 7.0f, -6.8f, UV_CORNER_BRACKET, light, overlay);
        }

        // South-West Corner
        if (!s && !w) {
            emitBoxUniform(pose, m, consumer, -8.0f, 3.6f, 6.6f, -6.6f, 7.15f, 8.0f, UV_CORNER_BRACKET, light, overlay);
            renderCornerFoot(pose, m, consumer, -7.8f, 5.8f, light, overlay);
        } else if (s && w && !state.southWest) {
            emitBoxUniform(pose, m, consumer, -8.0f, 3.8f, 6.8f, -6.8f, 7.0f, 8.0f, UV_CORNER_BRACKET, light, overlay);
        }

        // South-East Corner
        if (!s && !e) {
            emitBoxUniform(pose, m, consumer, 6.6f, 3.6f, 6.6f, 8.0f, 7.15f, 8.0f, UV_CORNER_BRACKET, light, overlay);
            renderCornerFoot(pose, m, consumer, 5.8f, 5.8f, light, overlay);
        } else if (s && e && !state.southEast) {
            emitBoxUniform(pose, m, consumer, 6.8f, 3.8f, 6.8f, 8.0f, 7.0f, 8.0f, UV_CORNER_BRACKET, light, overlay);
        }

        // 5. UNDER-DECK INVERTER CHASSIS, HEATSINK FINS & INTERCONNECT CONDUITS
        emitBoxUniform(pose, m, consumer, -4.2f, 1.2f, -4.2f, 4.2f, 4.0f, 4.2f, UV_INVERTER_HOUSING, light, overlay);
        emitBoxUniform(pose, m, consumer, -2.2f, 0.0f, -2.2f, 2.2f, 1.2f, 2.2f, UV_CORNER_BRACKET, light, overlay);

        // Cooling Heatsink Fins under Inverter
        for (int i = -3; i <= 3; i += 2) {
            emitBoxUniform(pose, m, consumer, i - 0.35f, 0.7f, -3.8f, i + 0.35f, 1.2f, 3.8f, UV_MOUNTING_STRUT, light, overlay);
        }

        // Structural Interconnect Trusses & Bus Conduits to connected neighbors
        if (n) {
            emitBoxUniform(pose, m, consumer, -3.2f, 2.2f, -8.0f, -2.0f, 3.8f, -4.2f, UV_MOUNTING_STRUT, light, overlay);
            emitBoxUniform(pose, m, consumer,  2.0f, 2.2f, -8.0f,  3.2f, 3.8f, -4.2f, UV_MOUNTING_STRUT, light, overlay);
            emitBoxUniform(pose, m, consumer, -0.8f, 1.6f, -8.0f,  0.8f, 2.8f, -4.2f, UV_CORNER_BRACKET, light, overlay);
        }
        if (s) {
            emitBoxUniform(pose, m, consumer, -3.2f, 2.2f, 4.2f, -2.0f, 3.8f, 8.0f, UV_MOUNTING_STRUT, light, overlay);
            emitBoxUniform(pose, m, consumer,  2.0f, 2.2f, 4.2f,  3.2f, 3.8f, 8.0f, UV_MOUNTING_STRUT, light, overlay);
            emitBoxUniform(pose, m, consumer, -0.8f, 1.6f, 4.2f,  0.8f, 2.8f, 8.0f, UV_CORNER_BRACKET, light, overlay);
        }
        if (w) {
            emitBoxUniform(pose, m, consumer, -8.0f, 2.2f, -3.2f, -4.2f, 3.8f, -2.0f, UV_MOUNTING_STRUT, light, overlay);
            emitBoxUniform(pose, m, consumer, -8.0f, 2.2f,  2.0f, -4.2f, 3.8f,  3.2f, UV_MOUNTING_STRUT, light, overlay);
            emitBoxUniform(pose, m, consumer, -8.0f, 1.6f, -0.8f, -4.2f, 2.8f,  0.8f, UV_CORNER_BRACKET, light, overlay);
        }
        if (e) {
            emitBoxUniform(pose, m, consumer, 4.2f, 2.2f, -3.2f, 8.0f, 3.8f, -2.0f, UV_MOUNTING_STRUT, light, overlay);
            emitBoxUniform(pose, m, consumer, 4.2f, 2.2f,  2.0f, 8.0f, 3.8f,  3.2f, UV_MOUNTING_STRUT, light, overlay);
            emitBoxUniform(pose, m, consumer, 4.2f, 1.6f, -0.8f, 8.0f, 2.8f,  0.8f, UV_CORNER_BRACKET, light, overlay);
        }
    }

    private static void renderCornerFoot(
            PoseStack.Pose pose,
            Matrix4f m,
            VertexConsumer consumer,
            float x0,
            float z0,
            int light,
            int overlay
    ) {
        emitBoxUniform(pose, m, consumer, x0 + 0.2f, 0.8f, z0 + 0.2f, x0 + 1.8f, 3.8f, z0 + 1.8f, UV_MOUNTING_STRUT, light, overlay);
        emitBoxUniform(pose, m, consumer, x0, 0.0f, z0, x0 + 2.0f, 0.8f, z0 + 2.0f, UV_RUBBER_FOOT, light, overlay);
    }

    private static void emitBoxUniform(
            PoseStack.Pose pose,
            Matrix4f matrix,
            VertexConsumer consumer,
            float x0, float y0, float z0,
            float x1, float y1, float z1,
            UVRect uv,
            int light,
            int overlay
    ) {
        emitBox(pose, matrix, consumer, x0, y0, z0, x1, y1, z1, uv, uv, uv, uv, uv, uv, light, overlay);
    }

    private static void emitBox(
            PoseStack.Pose pose,
            Matrix4f matrix,
            VertexConsumer consumer,
            float px0, float py0, float pz0,
            float px1, float py1, float pz1,
            UVRect north, UVRect south,
            UVRect west, UVRect east,
            UVRect up, UVRect down,
            int light,
            int overlay
    ) {
        float x0 = px0 / 16.0f;
        float y0 = py0 / 16.0f;
        float z0 = pz0 / 16.0f;
        float x1 = px1 / 16.0f;
        float y1 = py1 / 16.0f;
        float z1 = pz1 / 16.0f;

        // North (Z-)
        emitQuad(pose, matrix, consumer, x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0, north, 0.0f, 0.0f, -1.0f, light, overlay);
        // South (Z+)
        emitQuad(pose, matrix, consumer, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1, south, 0.0f, 0.0f, 1.0f, light, overlay);
        // West (X-)
        emitQuad(pose, matrix, consumer, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1, west, -1.0f, 0.0f, 0.0f, light, overlay);
        // East (X+)
        emitQuad(pose, matrix, consumer, x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0, east, 1.0f, 0.0f, 0.0f, light, overlay);
        // Up (Y+)
        emitQuad(pose, matrix, consumer, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, up, 0.0f, 1.0f, 0.0f, light, overlay);
        // Down (Y-)
        emitQuad(pose, matrix, consumer, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, down, 0.0f, -1.0f, 0.0f, light, overlay);
    }

    private static void emitQuad(
            PoseStack.Pose pose,
            Matrix4f matrix,
            VertexConsumer consumer,
            float xA, float yA, float zA,
            float xB, float yB, float zB,
            float xC, float yC, float zC,
            float xD, float yD, float zD,
            UVRect uv,
            float nx, float ny, float nz,
            int light,
            int overlay
    ) {
        consumer.addVertex(matrix, xA, yA, zA)
                .setColor(255, 255, 255, 255)
                .setUv(uv.u0(), uv.v0())
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);

        consumer.addVertex(matrix, xB, yB, zB)
                .setColor(255, 255, 255, 255)
                .setUv(uv.u0(), uv.v1())
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);

        consumer.addVertex(matrix, xC, yC, zC)
                .setColor(255, 255, 255, 255)
                .setUv(uv.u1(), uv.v1())
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);

        consumer.addVertex(matrix, xD, yD, zD)
                .setColor(255, 255, 255, 255)
                .setUv(uv.u1(), uv.v0())
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}

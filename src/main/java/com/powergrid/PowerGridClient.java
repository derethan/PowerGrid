package com.powergrid;

import com.powergrid.client.renderer.PowerCableRenderer;
import com.powergrid.client.renderer.WireNodeRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = PowerGridMod.MODID, dist = Dist.CLIENT)
public class PowerGridClient {

    public PowerGridClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerRenderers);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        PowerGridMod.LOGGER.info("Power Grid client setup initialized.");
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(PowerGridMod.WIRE_CONNECTOR_BE.get(), WireNodeRenderer::new);
        event.registerBlockEntityRenderer(PowerGridMod.WIRE_RELAY_BE.get(), WireNodeRenderer::new);
        event.registerBlockEntityRenderer(PowerGridMod.BREAKER_SWITCH_BE.get(), WireNodeRenderer::new);
        event.registerBlockEntityRenderer(PowerGridMod.POWER_CABLE_BE.get(), PowerCableRenderer::new);
    }
}

package com.powergrid;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(value = PowerGridMod.MODID, dist = Dist.CLIENT)
public class PowerGridClient {

    public PowerGridClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        PowerGridMod.LOGGER.info("Power Grid client setup initialized.");
    }
}

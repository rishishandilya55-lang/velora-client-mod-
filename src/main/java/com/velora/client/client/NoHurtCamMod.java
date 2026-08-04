package com.velora.client.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoHurtCamMod implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("Velora");

    public static void init() {
        LOGGER.info("[Velora] NoHurtCam mod initialized (delegated to GameRendererMixin)");
        // NoHurtCam rendering tilt is cleanly handled via GameRendererMixin!
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}

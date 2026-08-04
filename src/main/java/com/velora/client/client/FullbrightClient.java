package com.velora.client.client;

import net.fabricmc.api.ClientModInitializer;

public class FullbrightClient implements ClientModInitializer {

    public static void init() {
        // Fullbright is handled cleanly via GameRendererMixin.getNightVisionStrength
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}

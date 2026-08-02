package com.example.fpsdisplay.client;

import net.fabricmc.api.ClientModInitializer;

public class NoHurtCamMod implements ClientModInitializer {

    public static void init() {
        // NoHurtCam rendering tilt is cleanly handled via GameRendererMixin!
    }

    @Override
    public void onInitializeClient() {
        init();
    }
}

package dev.mizuopti;

import net.fabricmc.api.ClientModInitializer;

public final class MizuOptiClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientEntityRenderConfig.load();
    }
}
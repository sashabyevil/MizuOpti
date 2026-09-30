package dev.mizuopti;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Mob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class ClientEntityRenderConfig {
    private static final int DEFAULT_MOB_RENDER_RANGE = 128;
    private static volatile int mobRenderRange = DEFAULT_MOB_RENDER_RANGE;

    private ClientEntityRenderConfig() {
    }

    static void load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("mizuopti-client.properties");
        Properties properties = new Properties();

        try {
            Files.createDirectories(configPath.getParent());
            if (Files.exists(configPath)) {
                try (InputStream input = Files.newInputStream(configPath)) {
                    properties.load(input);
                }
            }

            mobRenderRange = readInt(properties.getProperty("mob-render-range"));
            if (!properties.containsKey("mob-render-range")) {
                properties.setProperty("mob-render-range", Integer.toString(mobRenderRange));
                try (OutputStream output = Files.newOutputStream(configPath)) {
                    properties.store(output, "MizuOpti client entity rendering settings");
                }
            }

            MizuOpti.LOGGER.info("Mob render distance optimization enabled: {} blocks",
                    mobRenderRange == 0 ? "disabled" : mobRenderRange);
        } catch (IOException exception) {
            mobRenderRange = DEFAULT_MOB_RENDER_RANGE;
            MizuOpti.LOGGER.error("Could not read or create mizuopti-client.properties; using defaults", exception);
        }
    }

    public static boolean shouldCull(Mob mob, double cameraX, double cameraY, double cameraZ) {
        int range = mobRenderRange;
        return range > 0 && mob.distanceToSqr(cameraX, cameraY, cameraZ) > (double) range * range;
    }

    private static int readInt(String value) {
        if (value == null) {
            return DEFAULT_MOB_RENDER_RANGE;
        }

        try {
            return Math.clamp(Integer.parseInt(value.trim()), 0, 1024);
        } catch (NumberFormatException exception) {
            MizuOpti.LOGGER.warn("Invalid value for 'mob-render-range'; using {}", DEFAULT_MOB_RENDER_RANGE);
            return DEFAULT_MOB_RENDER_RANGE;
        }
    }
}
package dev.mizuopti;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.Mob;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class EntityOptimizationConfig {
    private static final int DEFAULT_PLAYER_RANGE = 32;
    private static final int DEFAULT_AI_INTERVAL = 4;
    private static volatile int playerRange = DEFAULT_PLAYER_RANGE;
    private static volatile int aiInterval = DEFAULT_AI_INTERVAL;

    private EntityOptimizationConfig() {
    }

    static void load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("mizuopti.properties");
        Properties properties = new Properties();

        try {
            Files.createDirectories(configPath.getParent());
            if (Files.exists(configPath)) {
                try (InputStream input = Files.newInputStream(configPath)) {
                    properties.load(input);
                }
            }

            playerRange = readInt(properties, "player-range", DEFAULT_PLAYER_RANGE, 0, 256);
            aiInterval = readInt(properties, "ai-interval", DEFAULT_AI_INTERVAL, 1, 20);

            if (!Files.exists(configPath)) {
                properties.setProperty("player-range", Integer.toString(playerRange));
                properties.setProperty("ai-interval", Integer.toString(aiInterval));
                try (OutputStream output = Files.newOutputStream(configPath)) {
                    properties.store(output, "MizuOpti entity AI settings");
                }
            }

            MizuOpti.LOGGER.info("Entity AI optimization enabled: player range {} blocks, AI interval {} ticks",
                    playerRange, aiInterval);
        } catch (IOException exception) {
            playerRange = DEFAULT_PLAYER_RANGE;
            aiInterval = DEFAULT_AI_INTERVAL;
            MizuOpti.LOGGER.error("Could not read or create mizuopti.properties; using defaults", exception);
        }
    }

    public static boolean shouldSkipAi(Mob mob) {
        int interval = aiInterval;
        if (interval <= 1 || mob.level().isClientSide()) {
            return false;
        }

        if (mob.level().getNearestPlayer(mob, playerRange) != null) {
            return false;
        }

        return Math.floorMod(mob.tickCount + mob.getId(), interval) != 0;
    }

    private static int readInt(Properties properties, String key, int fallback, int min, int max) {
        try {
            return Math.clamp(Integer.parseInt(properties.getProperty(key, Integer.toString(fallback)).trim()), min, max);
        } catch (NumberFormatException exception) {
            MizuOpti.LOGGER.warn("Invalid value for '{}'; using {}", key, fallback);
            return fallback;
        }
    }
}
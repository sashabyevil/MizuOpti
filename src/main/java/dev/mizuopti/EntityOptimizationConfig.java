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
    private static final int DEFAULT_MID_RANGE = 64;
    private static final int DEFAULT_MID_AI_INTERVAL = 2;
    private static final int DEFAULT_FAR_RANGE = 128;
    private static final int DEFAULT_AI_INTERVAL = 4;
    private static final int DEFAULT_FAR_AI_INTERVAL = 8;
    private static volatile int playerRange = DEFAULT_PLAYER_RANGE;
    private static volatile int midRange = DEFAULT_MID_RANGE;
    private static volatile int midAiInterval = DEFAULT_MID_AI_INTERVAL;
    private static volatile int farRange = DEFAULT_FAR_RANGE;
    private static volatile int aiInterval = DEFAULT_AI_INTERVAL;
    private static volatile int farAiInterval = DEFAULT_FAR_AI_INTERVAL;

    private EntityOptimizationConfig() {
    }

    static void load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve("mizuopti.properties");
        Properties properties = new Properties();

        try {
            Files.createDirectories(configPath.getParent());
            boolean configNeedsSave = !Files.exists(configPath);
            if (!configNeedsSave) {
                try (InputStream input = Files.newInputStream(configPath)) {
                    properties.load(input);
                }
            }

            playerRange = readInt(properties, "player-range", DEFAULT_PLAYER_RANGE, 0, 256);
            midRange = Math.max(playerRange,
                    readInt(properties, "mid-range", DEFAULT_MID_RANGE, 0, 256));
            midAiInterval = readInt(properties, "mid-ai-interval", DEFAULT_MID_AI_INTERVAL, 1, 20);
                farRange = Math.max(midRange,
                    readInt(properties, "far-range", DEFAULT_FAR_RANGE, 0, 256));
            aiInterval = readInt(properties, "ai-interval", DEFAULT_AI_INTERVAL, 1, 20);
                farAiInterval = readInt(properties, "far-ai-interval", DEFAULT_FAR_AI_INTERVAL, 1, 20);

            configNeedsSave |= addDefault(properties, "player-range", playerRange);
            configNeedsSave |= addDefault(properties, "mid-range", midRange);
            configNeedsSave |= addDefault(properties, "mid-ai-interval", midAiInterval);
                configNeedsSave |= addDefault(properties, "far-range", farRange);
            configNeedsSave |= addDefault(properties, "ai-interval", aiInterval);
                configNeedsSave |= addDefault(properties, "far-ai-interval", farAiInterval);

            if (configNeedsSave) {
                try (OutputStream output = Files.newOutputStream(configPath)) {
                    properties.store(output, "MizuOpti entity AI settings");
                }
            }

            MizuOpti.LOGGER.info(
                    "Entity AI optimization enabled: full AI within {} blocks, then intervals {} / {} / {} ticks through {} / {} blocks",
                    playerRange, midAiInterval, aiInterval, farAiInterval, midRange, farRange);
        } catch (IOException exception) {
            playerRange = DEFAULT_PLAYER_RANGE;
            midRange = DEFAULT_MID_RANGE;
            midAiInterval = DEFAULT_MID_AI_INTERVAL;
            farRange = DEFAULT_FAR_RANGE;
            aiInterval = DEFAULT_AI_INTERVAL;
            farAiInterval = DEFAULT_FAR_AI_INTERVAL;
            MizuOpti.LOGGER.error("Could not read or create mizuopti.properties; using defaults", exception);
        }
    }

    public static boolean shouldSkipAi(Mob mob) {
        int interval = aiInterval;
        if (interval <= 1 || mob.level().isClientSide()) {
            return false;
        }

        var nearestPlayer = mob.level().getNearestPlayer(mob, farRange);
        if (nearestPlayer == null) {
            return !isAiTick(mob, farAiInterval);
        }

        double distanceSquared = mob.distanceToSqr(nearestPlayer);
        if (distanceSquared <= (double) playerRange * playerRange) {
            return false;
        }

        if (distanceSquared <= (double) midRange * midRange) {
            return !isAiTick(mob, midAiInterval);
        }

        return !isAiTick(mob, interval);
    }

    private static boolean isAiTick(Mob mob, int interval) {
        return interval <= 1 || Math.floorMod(mob.tickCount + mob.getId(), interval) == 0;
    }

    private static boolean addDefault(Properties properties, String key, int value) {
        if (properties.containsKey(key)) {
            return false;
        }

        properties.setProperty(key, Integer.toString(value));
        return true;
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
package me.mubioh.thirdpersonnametags.client;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public final class NametagScaleConfig {

    public static final float MIN_SCALE = 0.1F;
    public static final float MAX_SCALE = 10.0F;
    public static final float DEFAULT_SCALE = 1.0F;

    private static final Logger LOGGER = LoggerFactory.getLogger("ThirdPersonNametags");
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("thirdpersonnametags.properties");
    private static final String SELF_KEY = "selfScale";
    private static final String OTHERS_KEY = "othersScale";

    private static volatile float selfScale = DEFAULT_SCALE;
    private static volatile float othersScale = DEFAULT_SCALE;

    private NametagScaleConfig() {
    }

    public static float selfScale() {
        return selfScale;
    }

    public static float othersScale() {
        return othersScale;
    }

    public static void setSelfScale(float scale) {
        selfScale = clamp(scale);
        saveAsync();
    }

    public static void setOthersScale(float scale) {
        othersScale = clamp(scale);
        saveAsync();
    }

    public static void load() {
        if (!Files.isRegularFile(CONFIG_PATH)) {
            saveAsync();
            return;
        }
        final Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            properties.load(reader);
        } catch (IOException exception) {
            LOGGER.error("Failed to read {}", CONFIG_PATH, exception);
            return;
        }
        selfScale = parse(properties.getProperty(SELF_KEY));
        othersScale = parse(properties.getProperty(OTHERS_KEY));
    }

    private static void saveAsync() {
        CompletableFuture.runAsync(NametagScaleConfig::save);
    }

    private static synchronized void save() {
        final Properties properties = new Properties();
        properties.setProperty(SELF_KEY, Float.toString(selfScale));
        properties.setProperty(OTHERS_KEY, Float.toString(othersScale));
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                properties.store(writer, "ThirdPersonNametags nametag scales (" + MIN_SCALE + " - " + MAX_SCALE + ")");
            }
        } catch (IOException exception) {
            LOGGER.error("Failed to write {}", CONFIG_PATH, exception);
        }
    }

    private static float parse(String value) {
        if (value == null) {
            return DEFAULT_SCALE;
        }
        try {
            return clamp(Float.parseFloat(value.trim()));
        } catch (NumberFormatException exception) {
            return DEFAULT_SCALE;
        }
    }

    private static float clamp(float scale) {
        if (!Float.isFinite(scale)) {
            return DEFAULT_SCALE;
        }
        return Math.clamp(scale, MIN_SCALE, MAX_SCALE);
    }
}

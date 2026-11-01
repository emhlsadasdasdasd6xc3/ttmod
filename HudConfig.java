package com.example.teamtracker;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** HUD position and scale, saved to config/teamtracker-hud.properties. */
public final class HudConfig {
    public static final int DEFAULT_X = 6, DEFAULT_Y = 6;
    public static final float MIN_SCALE = 0.5f, MAX_SCALE = 3.0f;

    public static int x = DEFAULT_X;
    public static int y = DEFAULT_Y;
    public static float scale = 1.0f;

    /** Unscaled size of the HUD as last drawn; used to keep it on screen and for drag hit-testing. */
    public static int boxW = 100, boxH = 22;

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("teamtracker-hud.properties");

    private HudConfig() {}

    public static void load() {
        if (!Files.exists(FILE)) return;
        try (InputStream in = Files.newInputStream(FILE)) {
            Properties p = new Properties();
            p.load(in);
            x = Integer.parseInt(p.getProperty("x", String.valueOf(DEFAULT_X)));
            y = Integer.parseInt(p.getProperty("y", String.valueOf(DEFAULT_Y)));
            scale = Math.max(MIN_SCALE, Math.min(MAX_SCALE, Float.parseFloat(p.getProperty("scale", "1.0"))));
        } catch (IOException | NumberFormatException ignored) {
            reset();
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("x", String.valueOf(x));
        p.setProperty("y", String.valueOf(y));
        p.setProperty("scale", String.valueOf(scale));
        try (OutputStream out = Files.newOutputStream(FILE)) {
            p.store(out, "Team Tracker HUD layout");
        } catch (IOException ignored) {}
    }

    public static void reset() {
        x = DEFAULT_X;
        y = DEFAULT_Y;
        scale = 1.0f;
    }

    /** Keeps the HUD fully inside a screen of the given (scaled) size. */
    public static void clamp(int screenW, int screenH) {
        int w = (int) Math.ceil(boxW * scale);
        int h = (int) Math.ceil(boxH * scale);
        x = Math.max(0, Math.min(x, Math.max(0, screenW - w)));
        y = Math.max(0, Math.min(y, Math.max(0, screenH - h)));
    }
}

package com.laggo.fauxsweeper.config;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Configuration {
    private static final Gson gson = new Gson().newBuilder().setPrettyPrinting().create();
    private final int boardWidth;
    private final int boardHeight;
    private final int mineCount;
    private final boolean timerEnabled;
    private final boolean useSetSeed;
    private final long setSeed;
    private final double guiScale;
    private transient File file;

    Configuration(File file, int boardWidth, int boardHeight, int mineCount, boolean timerEnabled, boolean useSetSeed, long setSeed, double guiScale) {
        this.file = file;

        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.mineCount = mineCount;
        this.timerEnabled = timerEnabled;
        this.useSetSeed = useSetSeed;
        this.setSeed = setSeed;
        this.guiScale = guiScale;
    }

    public static Configuration fromFile(File file) {
        try {
            if (file.createNewFile()) {
                return defaultConfiguration(file);
            }

            Configuration configOut = gson.fromJson(new FileReader(file), Configuration.class);
            if (configOut == null) {
                return defaultConfiguration(file);
            }

            if (!configOut.isValid()) {
                throw new RuntimeException("invalid config!");
            }

            configOut.setFile(file);
            return configOut;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Configuration defaultConfiguration(File file) {
        return new Configuration(file, 10, 10, 10, true, false, 69420, 1.5d);
    }

    public boolean isValid() {
        return this.boardWidth > 0 && this.boardHeight > 0 && this.mineCount > 0 && this.mineCount <= this.boardWidth * this.boardHeight && this.guiScale > 0;
    }

    public void syncToFile() {
        // it should exist already
        try (FileWriter writer = new FileWriter(this.file)) {
            writer.write(gson.toJson(this));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getMineCount() {
        return this.mineCount;
    }

    public int getBoardHeight() {
        return this.boardHeight;
    }

    public int getBoardWidth() {
        return this.boardWidth;
    }

    public boolean isTimerEnabled() {
        return this.timerEnabled;
    }

    public boolean usesSetSeed() {
        return this.useSetSeed;
    }

    public long getSetSeed() {
        return this.setSeed;
    }

    public double getGuiScale() {
        return this.guiScale;
    }

    public void setFile(File file) {
        this.file = file;
    }
}

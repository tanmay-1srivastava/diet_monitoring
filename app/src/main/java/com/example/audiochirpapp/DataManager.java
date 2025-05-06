package com.example.audiochirpapp;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class for managing data storage
 */
public class DataManager {
    private static final String TAG = "DataManager";
    private static final String DIRECTORY_NAME = "AudioChirpData";

    private String baseFilename;
    private File chirpParamsFile;
    private File recordedDataFile;
    private FileWriter chirpParamsWriter;
    private FileWriter recordedDataWriter;
    private long startTimeMs;

    /**
     * Initializes the data manager with base filename
     *
     * @param baseFilename Base name for output files
     */
    public void initialize(String baseFilename) {
        this.baseFilename = baseFilename;
        this.startTimeMs = System.currentTimeMillis();

        // Create directory if it doesn't exist
        File directory = new File(Environment.getExternalStorageDirectory(), DIRECTORY_NAME);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "Failed to create directory: " + directory.getAbsolutePath());
                return;
            }
        }

        // Create timestamp for file names
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());

        // Create output files
        chirpParamsFile = new File(directory, baseFilename + "_params_" + timestamp + ".csv");
        recordedDataFile = new File(directory, baseFilename + "_recording_" + timestamp + ".csv");

        try {
            // Initialize writers
            chirpParamsWriter = new FileWriter(chirpParamsFile);
            recordedDataWriter = new FileWriter(recordedDataFile);

            // Write headers
            chirpParamsWriter.write("timestamp,eventType,leftFreq,leftBw,rightFreq,rightBw,duration\n");
            recordedDataWriter.write("relativeTimeMs,audioValue\n");

        } catch (IOException e) {
            Log.e(TAG, "Error initializing file writers", e);
        }
    }

    /**
     * Logs chirp parameters to CSV
     *
     * @param leftParams Left channel parameters
     * @param rightParams Right channel parameters
     */
    public void logChirpParameters(ChirpParams leftParams, ChirpParams rightParams) {
        if (chirpParamsWriter == null) {
            return;
        }

        try {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date());
            String line = String.format(Locale.US, "%s,CHIRP,%d,%d,%d,%d,%d\n",
                    timestamp,
                    leftParams.getCenterFrequency(),
                    leftParams.getBandwidth(),
                    rightParams.getCenterFrequency(),
                    rightParams.getBandwidth(),
                    leftParams.getDuration());

            chirpParamsWriter.write(line);
            chirpParamsWriter.flush();

        } catch (IOException e) {
            Log.e(TAG, "Error writing chirp parameters", e);
        }
    }

    /**
     * Saves recorded audio data to CSV
     *
     * @param data Audio data as short array
     * @param length Number of samples to save
     */
    public void saveRecordedData(short[] data, int length) {
        if (recordedDataWriter == null) {
            return;
        }

        try {
            long currentTimeMs = System.currentTimeMillis();
            long relativeTimeMs = currentTimeMs - startTimeMs;

            // For efficiency, use StringBuilder to batch writes
            StringBuilder sb = new StringBuilder();

            // Save every sample
            for (int i = 0; i < length; i++) {
                if (i < data.length) {
                    // Calculate precise timestamp for each sample
                    long sampleTimeMs = relativeTimeMs + i * 1000 / 44100;

                    sb.append(sampleTimeMs)
                            .append(",")
                            .append(data[i])
                            .append("\n");
                }
            }

            recordedDataWriter.write(sb.toString());
            recordedDataWriter.flush(); // Ensure data is written immediately

        } catch (IOException e) {
            Log.e(TAG, "Error writing recorded data", e);
        }
    }

    /**
     * Finalizes and closes all file writers
     */
    public void finalize() {
        try {
            if (chirpParamsWriter != null) {
                chirpParamsWriter.close();
                chirpParamsWriter = null;
            }

            if (recordedDataWriter != null) {
                recordedDataWriter.close();
                recordedDataWriter = null;
            }

        } catch (IOException e) {
            Log.e(TAG, "Error closing file writers", e);
        }
    }

    /**
     * Gets the output directory path
     *
     * @return Path to output directory
     */
    public String getOutputDirectory() {
        File directory = new File(Environment.getExternalStorageDirectory(), DIRECTORY_NAME);
        return directory.getAbsolutePath();
    }
}
package com.example.audiochirpapp;

/**
 * Class that holds parameters for a chirp signal
 */
public class ChirpParams {
    private int centerFrequency;    // Hz
    private int bandwidth;          // Hz
    private int duration;           // ms

    public ChirpParams(int centerFrequency, int bandwidth, int duration) {
        this.centerFrequency = centerFrequency;
        this.bandwidth = bandwidth;
        this.duration = duration;
    }

    public int getCenterFrequency() {
        return centerFrequency;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public int getDuration() {
        return duration;
    }

    // Calculate the starting frequency of the chirp
    public int getStartFrequency() {
        return Math.max(centerFrequency - (bandwidth / 2), 20); // Ensure min frequency is 20Hz
    }

    // Calculate the ending frequency of the chirp
    public int getEndFrequency() {
        return centerFrequency + (bandwidth / 2);
    }

    @Override
    public String toString() {
        return "ChirpParams{" +
                "centerFreq=" + centerFrequency +
                "Hz, bandwidth=" + bandwidth +
                "Hz, duration=" + duration +
                "ms}";
    }
}
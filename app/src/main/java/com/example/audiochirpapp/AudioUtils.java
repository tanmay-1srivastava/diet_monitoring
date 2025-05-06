package com.example.audiochirpapp;

/**
 * Utility class for generating audio signals
 */
public class AudioUtils {

    private static final int SAMPLE_RATE = 44100; // 44.1kHz for high quality audio

    /**
     * Generates a linear frequency chirp (sweep) signal
     *
     * @param startFreq Start frequency in Hz
     * @param endFreq End frequency in Hz
     * @param durationMs Duration in milliseconds
     * @return PCM 16-bit audio samples
     */
    public static short[] generateChirp(int startFreq, int endFreq, int durationMs) {
        int numSamples = (int) (SAMPLE_RATE * (durationMs / 1000.0));
        short[] samples = new short[numSamples];

        double samplingInterval = 1.0 / SAMPLE_RATE;
        double frequencySlope = (endFreq - startFreq) / (durationMs / 1000.0);

        // Generate chirp using instantaneous frequency method
        double phase = 0.0;
        double instantFreq;

        for (int i = 0; i < numSamples; i++) {
            double timePoint = i * samplingInterval;

            // Calculate instantaneous frequency at this time point
            instantFreq = startFreq + frequencySlope * timePoint;

            // Update phase
            phase += 2 * Math.PI * instantFreq * samplingInterval;

            // Apply window (Hanning window) to reduce clicks at start/end
            double amplitude = 0.5 * (1 - Math.cos(2 * Math.PI * i / (numSamples - 1)));

            // Generate sample
            samples[i] = (short) (Short.MAX_VALUE * amplitude * Math.sin(phase));
        }

        return samples;
    }

    /**
     * Interleaves left and right channel samples into a stereo PCM buffer
     *
     * @param leftSamples Left channel samples
     * @param rightSamples Right channel samples
     * @return Interleaved stereo samples
     */
    public static short[] interleave(short[] leftSamples, short[] rightSamples) {
        // Ensure both channels have the same length
        int length = Math.min(leftSamples.length, rightSamples.length);

        // Create interleaved output buffer
        short[] interleavedSamples = new short[length * 2];

        // Interleave samples (Left Right Left Right...)
        for (int i = 0; i < length; i++) {
            interleavedSamples[i * 2] = leftSamples[i];        // Left sample
            interleavedSamples[i * 2 + 1] = rightSamples[i];   // Right sample
        }

        return interleavedSamples;
    }

    /**
     * Convert a short array to a byte array (PCM 16-bit format)
     *
     * @param shorts Array of short samples
     * @return Array of bytes in PCM format
     */
    public static byte[] shortsToBytes(short[] shorts) {
        byte[] bytes = new byte[shorts.length * 2];

        for (int i = 0; i < shorts.length; i++) {
            // Little endian format
            bytes[i * 2] = (byte) (shorts[i] & 0xff);
            bytes[i * 2 + 1] = (byte) ((shorts[i] >> 8) & 0xff);
        }

        return bytes;
    }

    /**
     * Convert a byte array to a short array (PCM 16-bit format)
     *
     * @param bytes Array of bytes in PCM format
     * @return Array of short samples
     */
    public static short[] bytesToShorts(byte[] bytes) {
        short[] shorts = new short[bytes.length / 2];

        for (int i = 0; i < shorts.length; i++) {
            // Little endian format
            shorts[i] = (short) ((bytes[i * 2] & 0xFF) | (bytes[i * 2 + 1] << 8));
        }

        return shorts;
    }
}
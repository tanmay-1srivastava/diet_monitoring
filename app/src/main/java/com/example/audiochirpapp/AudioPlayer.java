package com.example.audiochirpapp;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

/**
 * Class for playing audio chirps
 */
public class AudioPlayer {
    private static final int SAMPLE_RATE = 44100; // 44.1kHz
    private AudioTrack audioTrack;
    private boolean isPlaying = false;
    private DataManager dataManager;

    /**
     * Sets the DataManager for saving transmitted signals
     *
     * @param dataManager DataManager instance to use
     */
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Plays a chirp with different parameters for left and right channels
     *
     * @param leftParams Parameters for left channel chirp
     * @param rightParams Parameters for right channel chirp
     */
    public void playChirp(ChirpParams leftParams, ChirpParams rightParams) {
        if (isPlaying) {
            stopPlaying();
        }

        // Generate chirp signals for each channel
        short[] leftSamples = AudioUtils.generateChirp(
                leftParams.getStartFrequency(),
                leftParams.getEndFrequency(),
                leftParams.getDuration());

        short[] rightSamples = AudioUtils.generateChirp(
                rightParams.getStartFrequency(),
                rightParams.getEndFrequency(),
                rightParams.getDuration());

        // Save the transmitted signal if DataManager is available
        if (dataManager != null) {
            dataManager.saveTransmittedData(leftSamples, rightSamples);
        }

        // Interleave samples for stereo output
        short[] stereoSamples = AudioUtils.interleave(leftSamples, rightSamples);

        // Convert to bytes for AudioTrack
        byte[] audioData = AudioUtils.shortsToBytes(stereoSamples);

        // Calculate buffer size based on duration
        int bufferSize = audioData.length;

        // Initialize AudioTrack
        initializeAudioTrack(bufferSize);

        // Write audio data to AudioTrack
        audioTrack.write(audioData, 0, audioData.length);

        // Start playback
        audioTrack.play();
        isPlaying = true;
    }

    /**
     * Initializes the AudioTrack with proper configuration
     *
     * @param bufferSize Size of the audio buffer
     */
    private void initializeAudioTrack(int bufferSize) {
        int minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

        // Ensure buffer size is adequate
        bufferSize = Math.max(bufferSize, minBufferSize);

        // Create AudioTrack instance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For modern Android versions
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            AudioFormat audioFormat = new AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build();

            audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setAudioFormat(audioFormat)
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build();
        } else {
            // For older Android versions
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize,
                    AudioTrack.MODE_STATIC);
        }
    }

    /**
     * Stops audio playback and releases resources
     */
    public void stopPlaying() {
        if (audioTrack != null) {
            try {
                if (isPlaying) {
                    audioTrack.stop();
                }
                audioTrack.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            audioTrack = null;
            isPlaying = false;
        }
    }
}
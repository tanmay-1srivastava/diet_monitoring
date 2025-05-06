package com.example.audiochirpapp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Class for recording audio from the microphone
 */
public class AudioRecorder {
    private static final int SAMPLE_RATE = 44100; // 44.1kHz
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;
    private boolean isRecording = false;
    private Thread recordingThread;

    /**
     * Starts recording audio from the microphone
     *
     * @param dataManager DataManager to save recorded data
     */
    public void startRecording(DataManager dataManager) {
        if (isRecording) {
            return;
        }

        // Calculate buffer size
        int minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT);

        // Add some extra buffer space
        int bufferSize = minBufferSize * 2;

        try {
            // Initialize AudioRecord
            audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    CHANNEL_CONFIG,
                    AUDIO_FORMAT,
                    bufferSize);

            // Start recording
            audioRecord.startRecording();
            isRecording = true;

            // Start a thread to read and save recorded data
            recordingThread = new Thread(() -> {
                byte[] buffer = new byte[bufferSize];

                while (isRecording) {
                    // Read audio data
                    int bytesRead = audioRecord.read(buffer, 0, buffer.length);

                    if (bytesRead > 0) {
                        // Convert to shorts for easier processing
                        short[] audioData = AudioUtils.bytesToShorts(buffer);

                        // Save data with timestamp
                        dataManager.saveRecordedData(audioData, bytesRead / 2);
                    }
                }
            });

            recordingThread.start();

        } catch (Exception e) {
            e.printStackTrace();
            stopRecording();
        }
    }

    /**
     * Stops recording and releases resources
     */
    public void stopRecording() {
        isRecording = false;

        if (recordingThread != null) {
            try {
                recordingThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recordingThread = null;
        }

        if (audioRecord != null) {
            try {
                audioRecord.stop();
                audioRecord.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            audioRecord = null;
        }
    }
}
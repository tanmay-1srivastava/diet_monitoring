package com.example.audiochirpapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 200;
    private static final String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    // UI elements
    private SeekBar leftFreqSeekBar, leftBwSeekBar;
    private SeekBar rightFreqSeekBar, rightBwSeekBar;
    private SeekBar durationSeekBar;

    private EditText leftFreqValue, leftBwValue;
    private EditText rightFreqValue, rightBwValue;
    private EditText durationValue;
    private EditText outputFilename;

    private Button startButton, stopButton;
    private TextView statusText;

    // Audio control objects
    private AudioPlayer audioPlayer;
    private AudioRecorder audioRecorder;
    private DataManager dataManager;

    // Audio parameters
    private int leftFrequency = 1000;
    private int leftBandwidth = 500;
    private int rightFrequency = 2000;
    private int rightBandwidth = 500;
    private int duration = 1000;
    private String filename = "chirp_test";

    // State variables
    private boolean isRunning = false;
    private Thread processingThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        initializeUI();

        // Setup all listeners
        setupAllListeners();

        // Check permissions
        checkPermissions();

        // Initialize audio components
        audioPlayer = new AudioPlayer();
        audioRecorder = new AudioRecorder();
        dataManager = new DataManager();

        // Add a toast for confirmation when app starts
        Toast.makeText(this, "Audio Chirp App Started", Toast.LENGTH_SHORT).show();
    }

    private void initializeUI() {
        // SeekBars
        leftFreqSeekBar = findViewById(R.id.leftFreqSeekBar);
        leftBwSeekBar = findViewById(R.id.leftBwSeekBar);
        rightFreqSeekBar = findViewById(R.id.rightFreqSeekBar);
        rightBwSeekBar = findViewById(R.id.rightBwSeekBar);
        durationSeekBar = findViewById(R.id.durationSeekBar);

        // EditTexts
        leftFreqValue = findViewById(R.id.leftFreqValue);
        leftBwValue = findViewById(R.id.leftBwValue);
        rightFreqValue = findViewById(R.id.rightFreqValue);
        rightBwValue = findViewById(R.id.rightBwValue);
        durationValue = findViewById(R.id.durationValue);
        outputFilename = findViewById(R.id.outputFilename);

        // Buttons
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);

        // TextView
        statusText = findViewById(R.id.statusText);
    }

    private void setupAllListeners() {
        // Button listeners
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChirp();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopChirp();
            }
        });

        // SeekBar listeners
        setupSeekBarListener(leftFreqSeekBar, leftFreqValue);
        setupSeekBarListener(leftBwSeekBar, leftBwValue);
        setupSeekBarListener(rightFreqSeekBar, rightFreqValue);
        setupSeekBarListener(rightBwSeekBar, rightBwValue);
        setupSeekBarListener(durationSeekBar, durationValue);

        // EditText listeners
        setupEditTextListener(leftFreqValue, leftFreqSeekBar, 20000);
        setupEditTextListener(leftBwValue, leftBwSeekBar, 5000);
        setupEditTextListener(rightFreqValue, rightFreqSeekBar, 20000);
        setupEditTextListener(rightBwValue, rightBwSeekBar, 5000);
        setupEditTextListener(durationValue, durationSeekBar, 5000);

        // Filename EditText listener
        outputFilename.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filename = s.toString();
            }
        });
    }

    private void setupSeekBarListener(final SeekBar seekBar, final EditText editText) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    editText.setText(String.valueOf(progress));
                    updateParameters();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupEditTextListener(final EditText editText, final SeekBar seekBar, final int maxValue) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int value = s.toString().isEmpty() ? 0 : Integer.parseInt(s.toString());
                    if (value > maxValue) {
                        value = maxValue;
                        editText.setText(String.valueOf(maxValue));
                    }
                    seekBar.setProgress(value);
                    updateParameters();
                } catch (NumberFormatException e) {
                    editText.setText("0");
                    seekBar.setProgress(0);
                    updateParameters();
                }
            }
        });
    }

    private void updateParameters() {
        // Update audio parameters from UI values
        try {
            leftFrequency = Integer.parseInt(leftFreqValue.getText().toString());
            leftBandwidth = Integer.parseInt(leftBwValue.getText().toString());
            rightFrequency = Integer.parseInt(rightFreqValue.getText().toString());
            rightBandwidth = Integer.parseInt(rightBwValue.getText().toString());
            duration = Integer.parseInt(durationValue.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void checkPermissions() {
        boolean hasAllPermissions = true;

        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false;
                break;
            }
        }

        if (!hasAllPermissions) {
            ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                Toast.makeText(MainActivity.this, "Permissions required", Toast.LENGTH_LONG).show();
                startButton.setEnabled(false);
            }
        }
    }

    private void startChirp() {
        // Check permissions first
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions required", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (isRunning) {
            return;
        }

        isRunning = true;
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        statusText.setText("Recording...");

        // Make sure parameters are up-to-date
        updateParameters();

        // Initialize DataManager with current timestamp and filename
        dataManager.initialize(filename);

        // Start recording audio
        audioRecorder.startRecording(dataManager);

        // Create and start the processing thread
        processingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Record ambient sound for a moment first
                    Thread.sleep(500);

                    // Create chirp parameters for both channels
                    final ChirpParams leftParams = new ChirpParams(leftFrequency, leftBandwidth, duration);
                    final ChirpParams rightParams = new ChirpParams(rightFrequency, rightBandwidth, duration);

                    // Log chirp parameters to CSV
                    dataManager.logChirpParameters(leftParams, rightParams);

                    // Play the chirp
                    audioPlayer.playChirp(leftParams, rightParams);

                    // Continue recording for a bit after chirp
                    Thread.sleep(duration + 1000);

                    // Stop everything on UI thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopChirp();
                            Toast.makeText(MainActivity.this, "Chirp completed", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            stopChirp();
                        }
                    });
                }
            }
        });

        processingThread.start();
        Toast.makeText(this, "Recording and chirp started", Toast.LENGTH_SHORT).show();
    }

    private void stopChirp() {
        if (!isRunning) {
            return;
        }

        isRunning = false;
        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        // Stop recording
        audioRecorder.stopRecording();

        // Stop audio playback
        audioPlayer.stopPlaying();

        // Finalize data saving
        dataManager.finalize();

        // Update UI
        statusText.setText("Ready");

        // Notify user
        Toast.makeText(this, "Data saved to " + dataManager.getOutputDirectory() + "/" + filename,
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure to clean up when the activity is destroyed
        if (isRunning) {
            stopChirp();
        }

        // Interrupt processing thread if it's still running
        if (processingThread != null && processingThread.isAlive()) {
            processingThread.interrupt();
        }
    }
}
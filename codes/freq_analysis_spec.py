from simple_parser import parse_lab_chart, rename_columns, fill_nan_with_mean
import sys
import os
import json
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from scipy import signal
import warnings
warnings.filterwarnings("ignore")

chips_data = parse_lab_chart('emg_data/chips.txt')
chips_data = rename_columns(chips_data)
chips_data['ear'] = fill_nan_with_mean(chips_data['ear'])
chips_data['tmj'] = fill_nan_with_mean(chips_data['tmj'])
chips_data['throat'] = fill_nan_with_mean(chips_data['throat'])


# first_chip_episode = 34 to 47 seconds
first_chip_episode = chips_data[(chips_data['Elapsed_Time_Sec'] > 34) & (chips_data['Elapsed_Time_Sec'] < 47)]
first_chip_episode = first_chip_episode.reset_index(drop=True)

# Create figure with 2 subplots
fig, ax = plt.subplots(2, 1, figsize=(8, 18), sharex=False)

# Plot the EMG data in the top subplot
ax[0].plot(first_chip_episode['Elapsed_Time_Sec'], first_chip_episode['ear'], label='Ear EMG', color='green')
ax[0].set_title('Ear EMG Signal')
ax[0].set_ylabel('EMG Value')
ax[0].set_xlabel('Time (s)')
ax[0].grid(True)

# Calculate the spectrogram
fs = 2000  # Sampling frequency (assumed to be 2000 Hz)
ear_signal = first_chip_episode['ear'].values

# Calculate the spectrogram using scipy's spectrogram function
f, t, Sxx = signal.spectrogram(ear_signal, fs, nperseg=32, noverlap=16)

# Map the time values to actual elapsed time
time_map = np.linspace(first_chip_episode['Elapsed_Time_Sec'].iloc[0], 
                       first_chip_episode['Elapsed_Time_Sec'].iloc[-1], 
                       len(t))

# Plot the spectrogram
im = ax[1].pcolormesh(time_map, f, 10 * np.log10(Sxx), shading='gouraud', cmap='viridis')
ax[1].set_title('Ear EMG Spectrogram')
ax[1].set_ylabel('Frequency (Hz)')
ax[1].set_xlabel('Time (s)')

# Adjust layout to prevent overlap
plt.tight_layout()

# Save and show
plt.savefig('plots/chips_ear_plot_spectrogram.pdf', bbox_inches='tight')
plt.show()
from simple_parser import parse_lab_chart, rename_columns, fill_nan_with_mean
import sys
import os
import json
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import warnings
warnings.filterwarnings("ignore")

chips_data = parse_lab_chart('emg_data/chips.txt')
chips_data = rename_columns(chips_data)
chips_data['ear'] = fill_nan_with_mean(chips_data['ear'])
chips_data['tmj'] = fill_nan_with_mean(chips_data['tmj'])
chips_data['throat'] = fill_nan_with_mean(chips_data['throat'])


def frequency_sepectrum(x, sf, scale):
    """
    Derive frequency spectrum of a signal from time domain
    :param x: signal in the time domain
    :param sf: sampling frequency
    :returns frequencies and their content distribution
    """
    x = x - np.average(x)  # zero-centering

    n = len(x)
    k = np.arange(n)
    tarr = n / float(sf)
    frqarr = k / float(tarr)  # two sides frequency range

    frqarr = frqarr[range(n // 2)]  # one side frequency range

    x = np.fft.fft(x) / n  # fft computing and normalization
    x = x[range(n // 2)]

    return frqarr, abs(x)*scale



# first_chip_episode = 34 to 47 seconds
first_chip_episode = chips_data[(chips_data['Elapsed_Time_Sec'] > 34) & (chips_data['Elapsed_Time_Sec'] < 40)]
first_chip_episode = first_chip_episode.reset_index(drop=True)


fig, ax = plt.subplots(2, 1, figsize=(10, 18), sharex=True)

# Plot the EMG data
ax[0].plot(first_chip_episode['Elapsed_Time_Sec'], first_chip_episode['ear'], label='Ear EMG', color='green')

# plot the frequency spectrum
x, y = frequency_sepectrum(first_chip_episode['ear'], 2000, 1)

ax[1].plot(x, y, label='Ear EMG Frequency Spectrum', color='green')

ax[0].set_title('Ear EMG')
ax[1].set_title('Ear EMG Frequency Spectrum')
ax[1].set_xlabel('Frequency (Hz)')
ax[0].set_ylabel('EMG Value')
ax[1].set_ylabel('EMG Value')

plt.savefig('plots/chips_plot_freq.pdf',bbox_inches='tight')
plt.show()
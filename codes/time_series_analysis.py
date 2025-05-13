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


print(chips_data.head())

fig, axs = plt.subplots(3, 1, figsize=(10, 18), sharex=True)

# Plot the EMG data
axs[0].plot(chips_data['Elapsed_Time_Sec'], chips_data['ear'], label='Ear EMG', color='green')
axs[1].plot(chips_data['Elapsed_Time_Sec'], chips_data['tmj'], label='TMJ EMG', color='green')
axs[2].plot(chips_data['Elapsed_Time_Sec'], chips_data['throat'], label='Throat EMG', color='green')

# Set titles and labels
axs[0].set_title('Ear EMG')
axs[1].set_title('TMJ EMG')
axs[2].set_title('Throat EMG')
axs[2].set_xlabel('Time (s)')
axs[0].set_ylabel('EMG Value')
axs[1].set_ylabel('EMG Value')
axs[2].set_ylabel('EMG Value')
# Rotate x-axis labels

plt.savefig('plots/chips_plot_time.pdf',bbox_inches='tight')

plt.show()
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
import re


# fill the NaN values with the mean of 3 previous values
def fill_nan_with_mean(data, window=3):
    for i in range(len(data)):
        if pd.isna(data[i]):
            start = max(0, i - window)
            end = min(len(data), i + window)
            mean_value = data[start:end].mean()
            data[i] = mean_value
    return data


# rename the column Channel 3 to throat, Channel 2 to tmj, Channel 1 to ear
def rename_columns(data):
    data = data.rename(columns={'Channel 1': 'ear', 'Channel 2': 'tmj', 'Channel 3': 'throat'})
    return data



def parse_lab_chart(file_path):
    # Read the file with error handling for encoding issues
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            lines = file.readlines()
    except UnicodeDecodeError:
        # Try with Latin-1 encoding which can handle most byte values
        with open(file_path, 'r', encoding='latin-1') as file:
            lines = file.readlines()
    
    # Extract metadata and find data start
    metadata = {}
    data_start_line = 0
    
    for i, line in enumerate(lines):
        line = line.strip()
        if not line:
            continue
            
        if '=' in line:
            parts = line.split('=', 1)
            if len(parts) >= 2:
                key, value = parts
                metadata[key.strip()] = value.strip()
        elif line[0].isdigit() or line.startswith('0.'):
            # This is where the data starts
            data_start_line = i
            break
    
    # Extract reference date/time from the ExcelDateTime field
    start_datetime = None
    if 'ExcelDateTime' in metadata:
        # Get the datetime part which is after the numeric value
        datetime_parts = metadata['ExcelDateTime'].split('\t')
        if len(datetime_parts) >= 2:
            date_time_str = datetime_parts[1].strip()
            try:
                start_datetime = datetime.strptime(date_time_str, '%m/%d/%Y %H:%M:%S.%f')
            except ValueError:
                print("Could not parse the datetime format:", date_time_str)
    
    if start_datetime is None:
        print("Using current time as fallback")
        start_datetime = datetime.now()
    
    # Extract channel titles
    channel_titles = []
    if 'ChannelTitle' in metadata:
        channel_titles = [title.strip() for title in metadata['ChannelTitle'].split('\t')[1:] if title.strip()]
    
    # Parse data section into DataFrame
    data_lines = lines[data_start_line:]
    data = []
    
    for line in data_lines:
        line = line.strip()
        if not line:
            continue
        
        values = line.split('\t')
        if len(values) > 1:
            # First value is elapsed time
            elapsed_time = float(values[0])
            # Rest are channel data
            channel_values = [float(v) if v.strip() else np.nan for v in values[1:]]
            data.append([elapsed_time] + channel_values)
    
    # Create DataFrame
    if data:
        # First column is elapsed time
        elapsed_times = [row[0] for row in data]
        
        # Channel data
        channel_data = [row[1:] for row in data]
        
        # Create column names
        if channel_titles and len(channel_titles) == len(channel_data[0]):
            column_names = channel_titles
        else:
            column_names = [f'Channel {i+1}' for i in range(len(channel_data[0]))]
        
        # Create the DataFrame
        df = pd.DataFrame(channel_data, columns=column_names)
        
        # Add elapsed time column
        df['Elapsed_Time_Sec'] = elapsed_times
        
        # Add exact timestamps - calculate from the start time plus elapsed seconds
        exact_timestamps = [start_datetime + timedelta(seconds=t) for t in elapsed_times]
        df['Timestamp'] = exact_timestamps
        
        # Reorder columns to have timestamps first
        cols = ['Timestamp', 'Elapsed_Time_Sec'] + column_names
        df = df[cols]
        
        return df
    
    return pd.DataFrame()

if __name__ == "__main__":
    # Example usage
    file_path = 'path_to_your_file.txt'
    df = parse_lab_chart(file_path)
    print(df.head())
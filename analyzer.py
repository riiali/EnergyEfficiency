import pandas as pd
import matplotlib.pyplot as plt

# Load the data from the CSV file
file_name = "C:\\Users\\alriz\\IdeaProjects\\EnergyComparisonProject\\energy_results.csv"  # Name of the generated CSV file
data = pd.read_csv(file_name)

# Display the first few rows of the data
print(data.head())

# Create a plot for CPU Load
plt.figure(figsize=(10, 6))
for pattern in data['Pattern'].unique():
    pattern_data = data[data['Pattern'] == pattern]
    plt.plot(pattern_data['Iteration'], pattern_data['CPU Load (%)'], label=f"CPU Load - {pattern}")

plt.title("CPU Load During Iterations")
plt.xlabel("Iteration")
plt.ylabel("CPU Load (%)")
plt.legend()
plt.grid()
plt.show()

# Create a plot for Execution Time
plt.figure(figsize=(10, 6))
for pattern in data['Pattern'].unique():
    pattern_data = data[data['Pattern'] == pattern]
    plt.plot(pattern_data['Iteration'], pattern_data['Execution Time (ms)'], label=f"Execution Time - {pattern}")

plt.title("Execution Time During Iterations")
plt.xlabel("Iteration")
plt.ylabel("Execution Time (ms)")
plt.legend()
plt.grid()
plt.show()

# Create a plot for Energy Consumed
plt.figure(figsize=(10, 6))
for pattern in data['Pattern'].unique():
    pattern_data = data[data['Pattern'] == pattern]
    plt.plot(pattern_data['Iteration'], pattern_data['Energy Consumed (J)'], label=f"Energy Consumed - {pattern}")

plt.title("Energy Consumed During Iterations")
plt.xlabel("Iteration")
plt.ylabel("Energy Consumed (J)")
plt.legend()
plt.grid()
plt.show()

package org.energyCons;

import org.energyCons.NoVisitor.WithoutVisitorPattern;
import org.energyCons.Visitor.VisitorPattern;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    private static final String SHELLY_IP = "192.168.1.182"; // Shelly Plug IP address

    public static void main(String[] args) throws IOException, InterruptedException {
        int iterations = 1000;
        int numNumbers = 1000;
        Random random = new Random();
        String fileName = "energy_shelly_results.csv";
        List<Integer> numbers = new ArrayList<>();

        for (int i = 0; i < numNumbers; i++) {
            numbers.add(random.nextInt(100));
        }

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.append("Pattern,Iteration,Apower (W),Voltage (V),Current (A),Aenergy Total (kWh),Aenergy By Minute\n");

            System.out.println("Running Validator...");
            double totalValidatorConsumption = runPattern("Validator", () -> VisitorPattern.sum(numbers), writer, iterations);

            // resetShellyEnergy();

            System.out.println("Running NonValidator...");
            double totalNonValidatorConsumption = runPattern("NonValidator", () -> WithoutVisitorPattern.sum(numbers), writer, iterations);

            System.out.println("Total Validator Consumption: " + totalValidatorConsumption + " mW");
            System.out.println("Total NonValidator Consumption: " + totalNonValidatorConsumption + " mW");
        }

        System.out.println("Results saved to " + fileName);
    }

    private static double runPattern(String patternName, Runnable task, FileWriter writer, int iterations) throws IOException, InterruptedException {
        double totalConsumption = 0.0;

        for (int i = 1; i <= iterations; i++) {
            System.out.println("Iteration: " + i);

            String startData = getShellyData();
            task.run();
            String endData = getShellyData();

            double apower = parseDoubleFromJson(endData, "\"apower\":");
            double voltage = parseDoubleFromJson(endData, "\"voltage\":");
            double current = parseDoubleFromJson(endData, "\"current\":");
            double aenergyTotal = parseDoubleFromJson(endData, "\"total\":");
            String aenergyByMinute = parseArrayFromJson(endData, "\"by_minute\":");

            double startTotalEnergy = parseDoubleFromJson(startData, "\"total\":");
            double iterationConsumption = aenergyTotal - startTotalEnergy;
            totalConsumption += iterationConsumption;

            writer.append(patternName + ",");
            writer.append(i + ",");
            writer.append(String.format("%.2f", apower) + ",");
            writer.append(String.format("%.2f", voltage) + ",");
            writer.append(String.format("%.2f", current) + ",");
            writer.append(String.format("%.3f", aenergyTotal) + ",");
            writer.append(aenergyByMinute + "\n");

            System.out.println("Iteration Consumption: " + iterationConsumption + " mW");
        }

        return totalConsumption;
    }

    private static String getShellyData() throws IOException, InterruptedException {
        String command = "curl -s http://" + SHELLY_IP + "/rpc/Shelly.GetStatus";

        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        process.waitFor();
        return response.toString();
    }

    private static double parseDoubleFromJson(String json, String key) {
        int startIndex = json.indexOf(key) + key.length();
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = json.indexOf("}", startIndex); // Handle last element case
        }
        return Double.parseDouble(json.substring(startIndex, endIndex).trim());
    }

    private static String parseArrayFromJson(String json, String key) {
        int startIndex = json.indexOf(key) + key.length();
        int endIndex = json.indexOf("]", startIndex) + 1; // Include the closing bracket
        return json.substring(startIndex, endIndex).trim();
    }
}

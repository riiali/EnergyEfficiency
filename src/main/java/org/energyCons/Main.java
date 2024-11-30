package org.energyCons;

import org.energyCons.NoVisitor.WithoutVisitorPattern;
import org.energyCons.Visitor.VisitorPattern;
import org.json.JSONObject;

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

            //resetShellyEnergy();

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

            JSONObject startData = getShellyData();
            task.run();
            JSONObject endData = getShellyData();

            double apower = endData.getJSONObject("switch:0").getDouble("apower");
            double voltage = endData.getJSONObject("switch:0").getDouble("voltage");
            double current = endData.getJSONObject("switch:0").getDouble("current");
            double aenergyTotal = endData.getJSONObject("switch:0").getJSONObject("aenergy").getDouble("total");
            String aenergyByMinute = endData.getJSONObject("switch:0").getJSONObject("aenergy").getJSONArray("by_minute").toString();

            double iterationConsumption = aenergyTotal - startData.getJSONObject("switch:0").getJSONObject("aenergy").getDouble("total");
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

    private static void resetShellyEnergy() throws IOException, InterruptedException {
        String command = "curl -s http://" + SHELLY_IP + "/rpc/Shelly.ResetTotalEnergy";
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        System.out.println("Shelly energy reset.");
    }

    private static JSONObject getShellyData() throws IOException, InterruptedException {
        String command = "curl -s http://" + SHELLY_IP + "/rpc/Shelly.GetStatus";

        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        process.waitFor();
        return new JSONObject(response.toString());
    }
}


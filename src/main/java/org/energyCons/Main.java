package org.energyCons;

import org.energyCons.NoVisitor.WithoutVisitorPattern;
import org.energyCons.Visitor.VisitorPattern;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    private static final String SHELLY_IP = "192.168.1.183"; // Shelly Plug IP address
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public static void main(String[] args) throws IOException, InterruptedException {
        int iterations = 5;
        int numNumbers = 1000;
        Random random = new Random();

        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < numNumbers; i++) {
            numbers.add(random.nextInt(100));
        }

        try (FileWriter validatorWriter = new FileWriter("validator_consumption.csv");
             FileWriter nonValidatorWriter = new FileWriter("non_validator_consumption.csv");
             FileWriter totalWriter = new FileWriter("total_consumption.csv")) {

            // Write CSV headers
            writeHeaders(validatorWriter);
            writeHeaders(nonValidatorWriter);
            writeTotalHeaders(totalWriter);

            System.out.println("Running Validator...");
            double totalValidatorConsumption = runPattern(
                    "Validator",
                    () -> VisitorPattern.sum(numbers),
                    validatorWriter,
                    iterations
            );

            System.out.println("Running NonValidator...");
            double totalNonValidatorConsumption = runPattern(
                    "NonValidator",
                    () -> WithoutVisitorPattern.sum(numbers),
                    nonValidatorWriter,
                    iterations
            );

            // Write total consumption
            totalWriter.append("Pattern;Total Consumption (Wh)\n");
            totalWriter.append("Validator;" + totalValidatorConsumption + "\n");
            totalWriter.append("NonValidator;" + totalNonValidatorConsumption + "\n");

            System.out.println("Total Validator Consumption: " + totalValidatorConsumption + " kWh");
            System.out.println("Total NonValidator Consumption: " + totalNonValidatorConsumption + " kWh");
        }
    }

    private static double runPattern(String patternName, Runnable task, FileWriter writer, int iterations) throws IOException, InterruptedException {
        double totalConsumption = 0.0;

        for (int i = 1; i <= iterations; i++) {
            System.out.println("Iteration: " + i);

            JSONObject startData = getShellyData();
            task.run();
            Thread.sleep(1000); // Delay to ensure energy data is updated
            JSONObject endData = getShellyData();

            // Extract nested data from switch:0
            JSONObject startSwitch = startData.getJSONObject("switch:0");
            JSONObject endSwitch = endData.getJSONObject("switch:0");

            double apower = startSwitch.optDouble("apower", 0.0); // Watt
            double voltage = startSwitch.optDouble("voltage", 0.0); // Volt
            double current = startSwitch.optDouble("current", 0.0); // Ampere
            double startTotalEnergy = startSwitch.getJSONObject("aenergy").optDouble("total", 0.0); // Convert Wh
            double endTotalEnergy = endSwitch.getJSONObject("aenergy").optDouble("total", 0.0); // Convert Wh
            double iterationConsumption = endTotalEnergy - startTotalEnergy;

            totalConsumption += iterationConsumption;

            writer.append(patternName).append(";")
                    .append(String.valueOf(i)).append(";")
                    .append(String.valueOf(apower)).append(";")
                    .append(String.valueOf(voltage)).append(";")
                    .append(String.valueOf(current)).append(";")
                    .append(String.valueOf(endTotalEnergy)).append(";")
                    .append(String.valueOf(iterationConsumption)).append("\n");

            System.out.println("Iteration Consumption: " + iterationConsumption + " kWh");
        }

        return totalConsumption;
    }

    private static JSONObject getShellyData() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + SHELLY_IP + "/rpc/Shelly.GetStatus"))
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return new JSONObject(response.body());
    }

    private static void writeHeaders(FileWriter writer) throws IOException {
        writer.append("Pattern;Iteration;Apower (W);Voltage (V);Current (A);Total Energy (Wh);Iteration Consumption (Wh)\n");
    }

    private static void writeTotalHeaders(FileWriter writer) throws IOException {
        writer.append("Pattern;Total Consumption (kWh)\n");
    }
}

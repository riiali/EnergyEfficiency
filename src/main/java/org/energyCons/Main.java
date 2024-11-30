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
    private static final String SHELLY_IP = "192.168.1.182"; // Cambia con l'indirizzo IP della tua Shelly Plug

    public static void main(String[] args) throws IOException, InterruptedException {
        int iterations = 1000; // Numero di iterazioni
        int numNumbers = 1000; // Numero di numeri
        Random random = new Random();
        String fileName = "energy_shelly_results.csv";
        List<Integer> numbers = new ArrayList<>();

        // Genera numeri casuali
        for (int i = 0; i < numNumbers; i++) {
            numbers.add(random.nextInt(100));
        }

        // Intestazione CSV
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.append("Pattern,Iteration,Consumption (mW)\n");

            // Azzera il consumo totale della Shelly
            resetShellyEnergy();

            // Esegui i test per Validator
            System.out.println("Running Validator...");
            double totalValidatorConsumption = runPattern("Validator", () -> VisitorPattern.sum(numbers), writer, iterations);

            // Azzera il consumo totale della Shelly
            resetShellyEnergy();

            // Esegui i test per NonValidator
            System.out.println("Running NonValidator...");
            double totalNonValidatorConsumption = runPattern("NonValidator", () -> WithoutVisitorPattern.sum(numbers), writer, iterations);

            // Stampa il consumo totale
            System.out.println("Total Validator Consumption: " + totalValidatorConsumption + " mW");
            System.out.println("Total NonValidator Consumption: " + totalNonValidatorConsumption + " mW");
        }

        System.out.println("Results saved to " + fileName);
    }

    private static double runPattern(String patternName, Runnable task, FileWriter writer, int iterations) throws IOException, InterruptedException {
        double totalConsumption = 0.0;

        for (int i = 1; i <= iterations; i++) {
            System.out.println("Iteration: " + i);

            // Ottieni il consumo iniziale
            double startEnergy = getShellyEnergyInMilliwatt();

            // Esegui il task
            task.run();

            // Ottieni il consumo finale
            double endEnergy = getShellyEnergyInMilliwatt();

            // Calcola il consumo per questa iterazione
            double iterationConsumption = endEnergy - startEnergy;
            totalConsumption += iterationConsumption;

            // Scrivi i risultati nel CSV
            writer.append(patternName + ",");
            writer.append(i + ",");
            writer.append(String.format("%.2f", iterationConsumption) + "\n");

            // Debug output
            System.out.println("Iteration Consumption: " + iterationConsumption + " mW");
        }

        return totalConsumption;
    }

    private static void resetShellyEnergy() throws IOException, InterruptedException {
        // Esegui il comando per azzerare il consumo totale
        String command = "curl -s http://" + SHELLY_IP + "/rpc/Shelly.ResetTotalEnergy";
        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();
        System.out.println("Shelly energy reset.");
    }

    private static double getShellyEnergyInMilliwatt() throws IOException, InterruptedException {
        // Comando curl per ottenere i dati di energia
        String command = "curl -s http://" + SHELLY_IP + "/rpc/Shelly.GetStatus";

        // Esegui il comando e leggi la risposta
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }

        process.waitFor(); // Aspetta che il processo termini

        // Estrai il valore dell'energia totale dal JSON
        String energyField = "\"total\":";
        int startIndex = response.indexOf(energyField) + energyField.length();
        int endIndex = response.indexOf(",", startIndex);
        double totalEnergyWh = Double.parseDouble(response.substring(startIndex, endIndex));

        // Converti da Wh a mW (1 Wh = 1000 mW/h)
        return totalEnergyWh * 1000.0;
    }
}

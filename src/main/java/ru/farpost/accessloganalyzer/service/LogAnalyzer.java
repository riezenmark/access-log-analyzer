package ru.farpost.accessloganalyzer.service;

import ru.farpost.accessloganalyzer.io.Arguments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

public class LogAnalyzer implements Analyzer {
    private static final DecimalFormat FORMATTER = new DecimalFormat("#0.0");

    public void analyze(final Arguments arguments) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            int availableLines = 0;
            int failureLines = 0;
            double availabilityLevel = 0;
            String endTime = "";
            boolean serviceIsCurrentlyAvailable = true;

            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(" ");
                String statusCode = columns[8];
                double responseTime = Double.parseDouble(columns[10]);

                if (serviceFailure(statusCode, responseTime, arguments.getResponseTime())) {
                    failureLines++;
                    if (serviceIsCurrentlyAvailable) {
                        printAvailabilityBorderTime(columns[3].split(":", 2)[1]);
                        serviceIsCurrentlyAvailable = false;
                    }
                } else {
                    if (!serviceIsCurrentlyAvailable) {
                        availableLines++;
                        double currentAvailabilityLevel = getAvailabilityLevel(availableLines, failureLines);
                        if (availabilityLevelIsAcceptable(currentAvailabilityLevel, arguments.getAvailability())) {
                            printAvailabilityBorderTime(endTime);
                            printFormattedAvailabilityLevel(availabilityLevel);
                            availableLines = 0;
                            failureLines = 0;
                            serviceIsCurrentlyAvailable = true;
                        }
                        availabilityLevel = currentAvailabilityLevel;
                        endTime = columns[3].split(":", 2)[1];
                    }
                }
            }
            if (!serviceIsCurrentlyAvailable) {
                printAvailabilityBorderTime(endTime);
                printFormattedAvailabilityLevel(availabilityLevel);
            }
        } catch (IOException ioe) {
            System.err.println("An error occurred during reading the log.");
            ioe.printStackTrace();
        }
    }

    private boolean serviceFailure(String statusCode, double responseTime, double acceptableResponseTime) {
        return statusCode.startsWith("5") || responseTime > acceptableResponseTime;
    }

    private double getAvailabilityLevel(double availableLines, int failureLines) {
        return availableLines / (availableLines + failureLines) * 100;
    }

    private boolean availabilityLevelIsAcceptable(double availability, double acceptableAvailability) {
        return availability >= acceptableAvailability;
    }

    private void printAvailabilityBorderTime(String time) {
        System.out.printf("%s ", time);
    }

    private void printFormattedAvailabilityLevel(double availabilityLevel) {
        System.out.println(FORMATTER.format(availabilityLevel));
    }
}

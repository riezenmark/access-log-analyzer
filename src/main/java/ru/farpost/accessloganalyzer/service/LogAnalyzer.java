package ru.farpost.accessloganalyzer.service;

import ru.farpost.accessloganalyzer.io.Arguments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class LogAnalyzer implements Analyzer {
    private static final DecimalFormat FORMATTER;

    static {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        FORMATTER = new DecimalFormat("#0.0", dfs);
    }

    private int availableLines = 0;
    private int failureLines = 0;
    private double availabilityLevel = 0;
    private boolean serviceIsCurrentlyAvailable = true;

    public void analyze(final Arguments arguments) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            String[] columns = {};
            String endTime = "";

            while ((line = reader.readLine()) != null) {
                columns = line.split(" ");
                String statusCode = columns[8];
                double responseTime = Double.parseDouble(columns[10]);

                if (serviceFailure(statusCode, responseTime, arguments.getResponseTime())) {
                    this.failureLines++;
                    if (serviceIsCurrentlyAvailable) {
                        String startTime = columns[3].split(":", 2)[1];
                        printAvailabilityBorderTime(startTime);
                        serviceIsCurrentlyAvailable = false;
                    }
                } else {
                    if (!serviceIsCurrentlyAvailable) {
                        this.availableLines++;
                        double currentAvailabilityLevel = getAvailabilityLevel(availableLines, failureLines);
                        String currentEndTime = columns[3].split(":", 2)[1];
                        if (availabilityLevelIsAcceptable(currentAvailabilityLevel, arguments.getAvailability())) {
                            printAvailabilityBorderTime(endTime);
                            printFormattedAvailabilityLevel(availabilityLevel);
                            resetLineCounters();
                            serviceIsCurrentlyAvailable = true;
                        }
                        availabilityLevel = currentAvailabilityLevel;
                        endTime = currentEndTime;
                    }
                }
            }
            if (!serviceIsCurrentlyAvailable) {
                printAvailabilityBorderTime(columns[3].split(":", 2)[1]);
                printFormattedAvailabilityLevel(getAvailabilityLevel(availableLines, failureLines));
            }
        } catch (IOException ioe) {
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

    private void resetLineCounters() {
        this.availableLines = 0;
        this.failureLines = 0;
    }
}

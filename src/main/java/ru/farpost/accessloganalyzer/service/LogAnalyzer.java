package ru.farpost.accessloganalyzer.service;

import ru.farpost.accessloganalyzer.io.Arguments;
import ru.farpost.accessloganalyzer.util.Printer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogAnalyzer implements Analyzer {
    private static final String ERROR_CODE_NUMBER = "5";

    private final ServiceState service = new ServiceState();

    public void analyze(Arguments arguments) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            String[] columns = {};

            while ((line = reader.readLine()) != null) {
                columns = line.split(" ");
                String statusCode = columns[8];
                double responseTime = Double.parseDouble(columns[10]);

                if (serviceFailure(statusCode, responseTime, arguments.getResponseTime())) {
                    processServiceFailureLine(columns[3]);
                } else if (!service.isCurrentlyAvailable()) {
                    processServiceAvailableLine(arguments, columns[3]);
                }
            }
            if (!service.isCurrentlyAvailable()) {
                Printer.printAvailabilitySectionBorderTime(columns[3].split(":", 2)[1]);
                Printer.printAvailabilityLevel(countAvailabilityLevel());
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void processServiceAvailableLine(Arguments arguments, String responseDateAndTime) {
        service.incrementAvailableLineCounter();
        double currentAvailabilityLevel = countAvailabilityLevel();
        String currentEndTime = extractCurrentResponseTime(responseDateAndTime);
        if (availabilityLevelIsAcceptable(currentAvailabilityLevel, arguments.getAvailability())) {
            Printer.printAvailabilitySectionBorderTime(service.getEndOfCurrentFailureSection());
            Printer.printAvailabilityLevel(service.getAvailabilityLevel());
            service.resetLineCounters();
            service.setCurrentlyAvailable(true);
        }
        service.setAvailabilityLevel(currentAvailabilityLevel);
        service.setEndOfCurrentFailureSection(currentEndTime);
    }

    private void processServiceFailureLine(String responseDateAndTime) {
        service.incrementFailureLineCounter();
        if (service.isCurrentlyAvailable()) {
            String startTime = extractCurrentResponseTime(responseDateAndTime);
            Printer.printAvailabilitySectionBorderTime(startTime);
            service.setCurrentlyAvailable(false);
        }
    }

    private boolean serviceFailure(String statusCode, double responseTime, double acceptableResponseTime) {
        return statusCode.startsWith(ERROR_CODE_NUMBER) || responseTime > acceptableResponseTime;
    }

    private boolean availabilityLevelIsAcceptable(double availability, double acceptableAvailability) {
        return availability >= acceptableAvailability;
    }

    private double countAvailabilityLevel() {
        return (double) service.getAvailableLines()
                / (service.getAvailableLines() + service.getFailureLines())
                * 100;
    }

    private String extractCurrentResponseTime(String responseDateAndTime) {
        return responseDateAndTime.split(":", 2)[1];
    }
}

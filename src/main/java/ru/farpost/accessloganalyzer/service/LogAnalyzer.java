package ru.farpost.accessloganalyzer.service;

import ru.farpost.accessloganalyzer.io.Arguments;
import ru.farpost.accessloganalyzer.util.LogLineParser;
import ru.farpost.accessloganalyzer.util.Printer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogAnalyzer implements Analyzer {
    private static final String ERROR_CODE_NUMBER = "5";

    private final ServiceState service = new ServiceState();
    private final Arguments arguments;

    public LogAnalyzer(Arguments arguments) {
        this.arguments = arguments;
    }

    public void analyze() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            reader.lines().forEach(this::processLogLine);

            if (!service.isCurrentlyAvailable()) {
                String endOfCurrentFailureSection = service.getEndOfCurrentFailureSection();
                double availabilityLevel = countAvailabilityLevel();
                Printer.printSectionEnding(endOfCurrentFailureSection, availabilityLevel);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void processLogLine(String line) {
        String[] columns = line.split(" ");
        String statusCode = columns[8];
        double responseTime = LogLineParser.extractResponseTime(columns[10]);

        if (serviceFailure(statusCode, responseTime, arguments.getAcceptableResponseTime())) {
            processServiceFailureLine(columns[3]);
        } else if (!service.isCurrentlyAvailable()) {
            processServiceAvailableLine();
        }
        if (!service.isCurrentlyAvailable()) {
            String requestDateAndTime = columns[3];
            String endOfCurrentFailureSection = LogLineParser.extractCurrentRequestTime(requestDateAndTime);
            service.setEndOfCurrentFailureSection(endOfCurrentFailureSection);
        }
    }

    private void processServiceAvailableLine() {
        service.incrementAvailableLineCounter();
        double currentAvailabilityLevel = countAvailabilityLevel();
        if (availabilityLevelIsAcceptable(currentAvailabilityLevel, arguments.getAcceptableAvailability())) {
            String endOfCurrentFailureSection = service.getEndOfCurrentFailureSection();
            double availabilityLevel = service.getAvailabilityLevel();
            Printer.printSectionEnding(endOfCurrentFailureSection, availabilityLevel);
            service.resetState();
        }
        service.setAvailabilityLevel(currentAvailabilityLevel);
    }

    private void processServiceFailureLine(String requestDateAndTime) {
        service.incrementFailureLineCounter();
        if (service.isCurrentlyAvailable()) {
            String startTime = LogLineParser.extractCurrentRequestTime(requestDateAndTime);
            Printer.printTime(startTime);
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
}

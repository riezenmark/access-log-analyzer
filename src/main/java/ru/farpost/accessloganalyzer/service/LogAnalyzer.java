package ru.farpost.accessloganalyzer.service;

import ru.farpost.accessloganalyzer.io.Arguments;
import ru.farpost.accessloganalyzer.util.Printer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogAnalyzer implements Analyzer {
    private static final String ERROR_CODE_NUMBER = "5";

    private final ServiceState service = new ServiceState();
    private final LogLineParser parser = new LogLineParser();

    private final Arguments arguments;

    public LogAnalyzer(Arguments arguments) {
        this.arguments = arguments;
    }

    public void analyze() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            reader.lines().forEach(this::processLogLine);

            if (!service.isCurrentlyAvailable()) {
                double finalAvailabilityLevel = countAvailabilityLevel();
                processEnding(finalAvailabilityLevel);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void processLogLine(String line) {
        parser.parseLine(line);
        String statusCode = parser.extractStatusCode();
        double responseTime = parser.extractResponseTime();

        if (serviceFailure(statusCode, responseTime, arguments.getAcceptableResponseTime())) {
            processServiceFailureLine();
        } else if (!service.isCurrentlyAvailable()) {
            processServiceAvailableLine();
        }
        if (!service.isCurrentlyAvailable()) {
            String requestDateAndTime = parser.extractRequestDateAndTime();
            String endOfCurrentFailureSection = parser.extractRequestTime(requestDateAndTime);
            service.setEndOfCurrentFailureSection(endOfCurrentFailureSection);
        }
    }

    private void processServiceFailureLine() {
        service.incrementFailureLineCounter();
        String requestDateAndTime = parser.extractRequestDateAndTime();
        if (service.isCurrentlyAvailable()) {
            String startTime = parser.extractRequestTime(requestDateAndTime);
            Printer.printTime(startTime);
            service.setCurrentlyAvailable(false);
        }
    }

    private void processServiceAvailableLine() {
        service.incrementAvailableLineCounter();
        double currentAvailabilityLevel = countAvailabilityLevel();
        if (availabilityLevelIsAcceptable(currentAvailabilityLevel, arguments.getAcceptableAvailability())) {
            double sectionAvailabilityLevel = service.getAvailabilityLevel();
            processEnding(sectionAvailabilityLevel);
            service.resetState();
        }
        service.setAvailabilityLevel(currentAvailabilityLevel);
    }

    private void processEnding(double availabilityLevel) {
        String endOfCurrentFailureSection = service.getEndOfCurrentFailureSection();
        Printer.printSectionEnding(endOfCurrentFailureSection, availabilityLevel);
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

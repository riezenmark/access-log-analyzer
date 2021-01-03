package ru.farpost.accessloganalyzer.service;

import ru.farpost.accessloganalyzer.io.Arguments;
import ru.farpost.accessloganalyzer.util.DecimalFormatter;
import ru.farpost.accessloganalyzer.util.LogParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogAnalyzer implements Analyzer {
    private static final String ERROR_CODE_NUMBER = "5";

    private final ServiceState serviceState = new ServiceState();
    private final Arguments arguments;

    public LogAnalyzer(Arguments arguments) {
        this.arguments = arguments;
    }

    public void analyze() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            reader.lines().forEach(this::processLogLine);

            if (!serviceState.isCurrentlyAvailable()) {
                double finalAvailabilityLevel = serviceState.countAvailabilityLevel();
                processEnding(finalAvailabilityLevel);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void processLogLine(String line) {
        LogLine logLine = LogParser.parseLine(line);
        String statusCode = logLine.getStatusCode();
        double responseTime = logLine.getResponseTime();

        if (serviceFailure(statusCode, responseTime, arguments.getAcceptableResponseTime())) {
            processServiceFailureLine(logLine);
        } else if (!serviceState.isCurrentlyAvailable()) {
            processServiceAvailableLine();
        }
        if (!serviceState.isCurrentlyAvailable()) {
            String endOfCurrentFailureSection = logLine.getRequestTime();
            serviceState.setEndOfCurrentFailureSection(endOfCurrentFailureSection);
        }
    }

    private void processServiceFailureLine(LogLine logLine) {
        serviceState.incrementFailureLineCounter();
        if (serviceState.isCurrentlyAvailable()) {
            String startTime = logLine.getRequestTime();
            System.out.printf("%s ", startTime);
            serviceState.setCurrentlyAvailable(false);
        }
    }

    private void processServiceAvailableLine() {
        serviceState.incrementAvailableLineCounter();
        double currentAvailabilityLevel = serviceState.countAvailabilityLevel();
        if (availabilityLevelIsAcceptable(currentAvailabilityLevel, arguments.getAcceptableAvailability())) {
            double sectionAvailabilityLevel = serviceState.getCurrentAvailabilityLevel();
            processEnding(sectionAvailabilityLevel);
            serviceState.resetState();
        }
        serviceState.setCurrentAvailabilityLevel(currentAvailabilityLevel);
    }

    private void processEnding(double availabilityLevel) {
        String endOfCurrentFailureSection = serviceState.getEndOfCurrentFailureSection();
        String formattedAvailabilityLevel = DecimalFormatter.format(availabilityLevel);
        System.out.printf("%s %s%n", endOfCurrentFailureSection, formattedAvailabilityLevel);
    }

    private boolean serviceFailure(String statusCode, double responseTime, double acceptableResponseTime) {
        return statusCode.startsWith(ERROR_CODE_NUMBER) || responseTime > acceptableResponseTime;
    }

    private boolean availabilityLevelIsAcceptable(double availability, double acceptableAvailability) {
        return availability >= acceptableAvailability;
    }
}

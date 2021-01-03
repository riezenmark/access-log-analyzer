package ru.farpost.accessloganalyzer.service;

import lombok.Cleanup;
import lombok.SneakyThrows;
import ru.farpost.accessloganalyzer.io.Arguments;
import ru.farpost.accessloganalyzer.util.DecimalFormatter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LogAnalyzer implements Analyzer {
    private static final String ERROR_CODE_NUMBER = "5";

    private final ServiceLogState serviceLogState = new ServiceLogState();
    private final Arguments arguments;

    public LogAnalyzer(Arguments arguments) {
        this.arguments = arguments;
    }

    @SneakyThrows
    public void analyze() {
        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            reader.lines().forEach(this::processLogLine);

            if (!serviceLogState.isCurrentlyAvailable()) {
                double finalAvailabilityLevel = serviceLogState.countAvailabilityLevel();
                printProcessedSectionEndingInfo(finalAvailabilityLevel);
            }
    }

    private void processLogLine(String line) {
        LogLine logLine = new LogLine(line);

        if (isServiceFailure(logLine, arguments.getAcceptableResponseTime())) {
            processLogFailureLine(logLine);
        } else if (!serviceLogState.isCurrentlyAvailable()) {
            processLogAvailableLine();
        }
        if (!serviceLogState.isCurrentlyAvailable()) {
            String endOfCurrentFailureSection = logLine.getRequestTime();
            serviceLogState.setEndOfCurrentFailureSection(endOfCurrentFailureSection);
        }
    }

    private void processLogFailureLine(LogLine logLine) {
        serviceLogState.incrementFailureLineCounter();
        if (serviceLogState.isCurrentlyAvailable()) {
            String startTime = logLine.getRequestTime();
            System.out.printf("%s ", startTime);
            serviceLogState.setCurrentlyAvailable(false);
        }
    }

    private void processLogAvailableLine() {
        serviceLogState.incrementAvailableLineCounter();
        double currentAvailabilityLevel = serviceLogState.countAvailabilityLevel();
        if (isAvailabilityLevelAcceptable(currentAvailabilityLevel, arguments.getAcceptableAvailability())) {
            printProcessedSectionEndingInfo(serviceLogState.getCurrentAvailabilityLevel());
            serviceLogState.resetState();
        }
        serviceLogState.setCurrentAvailabilityLevel(currentAvailabilityLevel);
    }

    private void printProcessedSectionEndingInfo(double availabilityLevel) {
        String endOfCurrentFailureSection = serviceLogState.getEndOfCurrentFailureSection();
        String formattedAvailabilityLevel = DecimalFormatter.format(availabilityLevel);
        System.out.printf("%s %s%n", endOfCurrentFailureSection, formattedAvailabilityLevel);
    }

    private boolean isServiceFailure(LogLine logLine, double acceptableResponseTime) {
        return logLine.getStatusCode().startsWith(ERROR_CODE_NUMBER)
                || logLine.getResponseTime() > acceptableResponseTime;
    }

    private boolean isAvailabilityLevelAcceptable(double availability, double acceptableAvailability) {
        return availability >= acceptableAvailability;
    }
}

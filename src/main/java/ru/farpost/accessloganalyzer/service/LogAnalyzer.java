package ru.farpost.accessloganalyzer.service;

import lombok.Cleanup;
import lombok.SneakyThrows;
import ru.farpost.accessloganalyzer.io.Arguments;
import ru.farpost.accessloganalyzer.util.DecimalFormatter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LogAnalyzer implements Analyzer {
    private static final String ERROR_CODE_NUMBER = "5";

    @SneakyThrows
    public void analyze(Arguments arguments) {
        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            ServiceLogState serviceLogState = new ServiceLogState();
            reader.lines()
                    .forEach(line -> processLogLine(line, serviceLogState, arguments));

            if (!serviceLogState.isCurrentlyAvailable()) {
                double finalAvailabilityLevel = serviceLogState.countAvailabilityLevel();
                printProcessedSectionEndingInfo(finalAvailabilityLevel, serviceLogState);
            }
    }

    private void processLogLine(String line, ServiceLogState serviceLogState, Arguments arguments) {
        LogLine logLine = new LogLine(line);

        if (isServiceFailure(logLine, arguments)) {
            processLogFailureLine(logLine, serviceLogState);
        } else if (!serviceLogState.isCurrentlyAvailable()) {
            processLogAvailableLine(serviceLogState, arguments);
        }
        if (!serviceLogState.isCurrentlyAvailable()) {
            String endOfCurrentFailureSection = logLine.getRequestTime();
            serviceLogState.setEndOfCurrentFailureSection(endOfCurrentFailureSection);
        }
    }

    private void processLogFailureLine(LogLine logLine, ServiceLogState serviceLogState) {
        serviceLogState.incrementFailureLineCounter();
        if (serviceLogState.isCurrentlyAvailable()) {
            String startTime = logLine.getRequestTime();
            System.out.printf("%s ", startTime);
            serviceLogState.setCurrentlyAvailable(false);
        }
    }

    private void processLogAvailableLine(ServiceLogState serviceLogState, Arguments arguments) {
        serviceLogState.incrementAvailableLineCounter();
        double currentAvailabilityLevel = serviceLogState.countAvailabilityLevel();
        if (isAvailabilityLevelAcceptable(currentAvailabilityLevel, arguments.getAcceptableAvailability())) {
            printProcessedSectionEndingInfo(serviceLogState.getCurrentAvailabilityLevel(), serviceLogState);
            serviceLogState.resetState();
        }
        serviceLogState.setCurrentAvailabilityLevel(currentAvailabilityLevel);
    }

    private void printProcessedSectionEndingInfo(double availabilityLevel, ServiceLogState serviceLogState) {
        String endOfCurrentFailureSection = serviceLogState.getEndOfCurrentFailureSection();
        String formattedAvailabilityLevel = DecimalFormatter.format(availabilityLevel);
        System.out.printf("%s %s%n", endOfCurrentFailureSection, formattedAvailabilityLevel);
    }

    private boolean isServiceFailure(LogLine logLine, Arguments arguments) {
        return logLine.getStatusCode().startsWith(ERROR_CODE_NUMBER)
                || logLine.getResponseTime() > arguments.getAcceptableResponseTime();
    }

    private boolean isAvailabilityLevelAcceptable(double availability, double acceptableAvailability) {
        return availability >= acceptableAvailability;
    }
}

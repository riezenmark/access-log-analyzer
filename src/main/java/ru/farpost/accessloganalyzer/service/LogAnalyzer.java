package ru.farpost.accessloganalyzer.service;

import lombok.Cleanup;
import lombok.SneakyThrows;
import net.jcip.annotations.ThreadSafe;
import ru.farpost.accessloganalyzer.io.Arguments;
import ru.farpost.accessloganalyzer.util.DecimalFormatter;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@ThreadSafe
public class LogAnalyzer implements Analyzer {
    private static final String ERROR_CODE_PREFIX = "5";

    @SneakyThrows
    public void analyze(Arguments arguments) {
        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        ServiceLogState serviceLogState = new ServiceLogState();
        reader.lines()
                .forEach(line -> processLogLine(line, serviceLogState, arguments));

        if (serviceLogState.isDenialSectionStarted()) {
            double finalAvailabilityLevel = serviceLogState.countAvailabilityLevel();
            printLineLogResult(serviceLogState, finalAvailabilityLevel);
        }
    }

    private void processLogLine(String line, ServiceLogState serviceLogState, Arguments arguments) {
        LogLine logLine = new LogLine(line);

        if (isServiceFailure(logLine, arguments.getAcceptableResponseTime())) {
            processLogFailureLine(logLine, serviceLogState);
        } else if (serviceLogState.isDenialSectionStarted()) {
            processLogAvailableLine(serviceLogState, arguments);
        }
        if (serviceLogState.isDenialSectionStarted()) {
            String endOfCurrentFailureSection = logLine.getRequestTime();
            serviceLogState.setEndOfCurrentFailureSection(endOfCurrentFailureSection);
        }
    }

    private void processLogFailureLine(LogLine logLine, ServiceLogState serviceLogState) {
        serviceLogState.incrementFailureLineCounter();
        if (!serviceLogState.isDenialSectionStarted()) {
            String startTime = logLine.getRequestTime();
            System.out.printf("%s ", startTime);
            serviceLogState.setDenialSectionStarted(true);
        }
    }

    private void processLogAvailableLine(ServiceLogState serviceLogState, Arguments arguments) {
        serviceLogState.incrementAvailableLineCounter();
        double currentAvailabilityLevel = serviceLogState.countAvailabilityLevel();
        if (isAvailabilityLevelAcceptable(currentAvailabilityLevel, arguments.getAcceptableAvailability())) {
            printLineLogResult(serviceLogState, serviceLogState.getCurrentAvailabilityLevel());
            serviceLogState.resetState();
        }
        serviceLogState.setCurrentAvailabilityLevel(currentAvailabilityLevel);
    }

    private void printLineLogResult(ServiceLogState serviceLogState, double availabilityLevel) {
        String endOfCurrentFailureSection = serviceLogState.getEndOfCurrentFailureSection();
        String formattedAvailabilityLevel = DecimalFormatter.format(availabilityLevel);
        System.out.printf("%s %s%n", endOfCurrentFailureSection, formattedAvailabilityLevel);
    }

    private boolean isServiceFailure(LogLine logLine, double acceptableResponseTime) {
        return logLine.getStatusCode().startsWith(ERROR_CODE_PREFIX)
                || logLine.getResponseTime() > acceptableResponseTime;
    }

    private boolean isAvailabilityLevelAcceptable(double availability, double acceptableAvailability) {
        return availability >= acceptableAvailability;
    }
}

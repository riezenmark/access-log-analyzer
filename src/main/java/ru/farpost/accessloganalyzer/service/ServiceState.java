package ru.farpost.accessloganalyzer.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceState {
    private int availableLines = 0;
    private int failureLines = 0;
    private boolean currentlyAvailable = true;
    private double availabilityLevel = 0;
    private String endOfCurrentFailureSection;

    public void incrementAvailableLineCounter() {
        availableLines++;
    }

    public void incrementFailureLineCounter() {
        failureLines++;
    }

    public void resetLineCounters() {
        availableLines = 0;
        failureLines = 0;
    }
}

package ru.farpost.accessloganalyzer.service;

public class LogLineParser {
    private String[] columns;

    public String extractRequestDateAndTime() {
        return columns[3].substring(1);
    }

    public String extractRequestTime(String requestDateAndTime) {
        return requestDateAndTime.split(":", 2)[1];
    }

    public double extractResponseTime() {
        return Double.parseDouble(columns[10]);
    }

    public void parseLine(String line) {
        this.columns = line.split(" ");
    }

    public String extractStatusCode() {
        return columns[8];
    }
}

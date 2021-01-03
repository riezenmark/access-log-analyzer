package ru.farpost.accessloganalyzer.util;

public class LogLineParser {
    public static String extractCurrentRequestTime(String responseDateAndTime) {
        return responseDateAndTime.split(":", 2)[1];
    }

    public static double extractResponseTime(String responseTime) {
        return Double.parseDouble(responseTime);
    }
}

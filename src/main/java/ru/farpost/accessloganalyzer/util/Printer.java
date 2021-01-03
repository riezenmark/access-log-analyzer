package ru.farpost.accessloganalyzer.util;

public class Printer {
    public static void printTime(String time) {
        System.out.printf("%s ", time);
    }

    public static void printSectionEnding(String endOfSectionTime, double availabilityLevel) {
        String formattedAvailabilityLevel = DecimalFormatter.format(availabilityLevel);
        System.out.printf("%s %s%n", endOfSectionTime, formattedAvailabilityLevel);
    }
}

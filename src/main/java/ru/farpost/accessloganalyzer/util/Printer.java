package ru.farpost.accessloganalyzer.util;

public class Printer {
    public static void printAvailabilitySectionBorderTime(String time) {
        System.out.printf("%s ", time);
    }

    public static void printAvailabilityLevel(double availabilityLevel) {
        String formattedAvailabilityLevel = DecimalFormatter.format(availabilityLevel);
        System.out.println(formattedAvailabilityLevel);
    }
}

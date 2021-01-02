package ru.farpost.accessloganalyzer.io.util;

import ru.farpost.accessloganalyzer.io.Arguments;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ArgumentsExtractor {
    private static final Arguments arguments = new Arguments();
    private static final Map<String, Consumer<String>> extractor = new HashMap<>();

    static {
        extractor.put("-u", ArgumentsExtractor::extractAvailability);
        extractor.put("-t", ArgumentsExtractor::extractAccessTime);
    }

    public static Arguments extract(String[] args) {
        for (int i = 0; i < args.length; i++) {
            extractor.get(args[i])
                    .accept(args[++i]);
        }
        return arguments;
    }

    private static void extractAvailability(String argument) throws NullPointerException, IllegalArgumentException {
        double availability = Double.parseDouble(argument);
        if (availability < 0 || availability > 100) {
            throw new IllegalArgumentException("-u must be a value in [0; 100].");
        } else {
            arguments.setAvailability(availability);
        }
    }

    private static void extractAccessTime(String argument) throws NullPointerException, IllegalArgumentException {
        double responseTime = Double.parseDouble(argument);
        if (responseTime <= 0) {
            throw new IllegalArgumentException("-t must be a value greater than 0.");
        } else {
            arguments.setResponseTime(responseTime);
        }
    }
}

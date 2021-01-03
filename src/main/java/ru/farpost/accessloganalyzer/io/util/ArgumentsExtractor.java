package ru.farpost.accessloganalyzer.io.util;

import lombok.experimental.UtilityClass;
import ru.farpost.accessloganalyzer.io.Arguments;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@UtilityClass
public class ArgumentsExtractor {
    private static final Map<String, BiConsumer<String, Arguments>> EXTRACTOR = new HashMap<>();

    static {
        EXTRACTOR.put("-u", ArgumentsExtractor::extractAvailability);
        EXTRACTOR.put("-t", ArgumentsExtractor::extractAccessTime);
    }

    public static Arguments extract(String[] args) {
        Arguments arguments = new Arguments();
        for (int i = 0; i < args.length; i++) {
            EXTRACTOR.get(args[i])
                    .accept(args[++i], arguments);
        }
        return arguments;
    }

    private static void extractAvailability(String argument, Arguments arguments) throws NullPointerException, IllegalArgumentException {
        double availability = Double.parseDouble(argument);
        if (availability >= 0 && availability <= 100) {
            arguments.setAcceptableAvailability(availability);
        } else {
            throw new IllegalArgumentException("-u must be a value in [0; 100].");
        }
    }

    private static void extractAccessTime(String argument, Arguments arguments) throws NullPointerException, IllegalArgumentException {
        double responseTime = Double.parseDouble(argument);
        if (responseTime > 0) {
            arguments.setAcceptableResponseTime(responseTime);
        } else {
            throw new IllegalArgumentException("-t must be a value greater than 0.");
        }
    }
}

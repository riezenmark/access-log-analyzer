package ru.farpost.accessloganalyzer.util;

import lombok.experimental.UtilityClass;
import ru.farpost.accessloganalyzer.service.LogLine;

@UtilityClass
public class LogParser {

    public static LogLine parseLine(String line) {
        String[] columns = line.split(" ");

        String requestTime = columns[3].split(":", 2)[1];
        String statusCode = columns[8];
        double responseTime = Double.parseDouble(columns[10]);

        return LogLine.builder()
                .requestTime(requestTime)
                .statusCode(statusCode)
                .responseTime(responseTime)
                .build();
    }
}

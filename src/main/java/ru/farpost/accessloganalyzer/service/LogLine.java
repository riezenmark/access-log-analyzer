package ru.farpost.accessloganalyzer.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogLine {
    private String requestTime;
    private double responseTime;
    private String statusCode;

    public LogLine(String line) {
        String[] columns = line.split(" ");
        requestTime = columns[3].split(":", 2)[1];
        statusCode = columns[8];
        responseTime = Double.parseDouble(columns[10]);
    }
}

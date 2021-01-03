package ru.farpost.accessloganalyzer.service;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LogLine {
    private String requestTime;
    private double responseTime;
    private String statusCode;
}

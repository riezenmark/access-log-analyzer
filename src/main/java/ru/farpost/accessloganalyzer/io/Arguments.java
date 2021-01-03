package ru.farpost.accessloganalyzer.io;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Arguments {
    private double acceptableAvailability;
    private double acceptableResponseTime;
}

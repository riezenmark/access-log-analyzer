package ru.farpost.accessloganalyzer.service;

import ru.farpost.accessloganalyzer.io.Arguments;

public interface Analyzer {
    void analyze(final Arguments arguments);
}

package ru.farpost.accessloganalyzer;

import ru.farpost.accessloganalyzer.io.util.ArgumentsExtractor;
import ru.farpost.accessloganalyzer.service.Analyzer;
import ru.farpost.accessloganalyzer.service.LogAnalyzer;

public class AccessLogAnalyzerApplication {
    public static void main(String[] args) {
        Analyzer analyzer = new LogAnalyzer();
        analyzer.analyze(ArgumentsExtractor.extract(args));
    }
}

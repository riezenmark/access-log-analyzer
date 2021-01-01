package ru.farpost.accessloganalyzer;

import ru.farpost.accessloganalyzer.io.Arguments;
import ru.farpost.accessloganalyzer.io.util.ArgumentsExtractor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AccessLogAnalyzerApplication {
    public static void main(String[] args) {
        Arguments arguments = ArgumentsExtractor.extract(args);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(" ");
                if (columns[8].startsWith("5") || Double.parseDouble(columns[10]) > arguments.getAccessTime()) {
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred during reading the log.");
            e.printStackTrace();
        }

    }
}

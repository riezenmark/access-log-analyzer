package ru.farpost.accessloganalyzer.util;

import lombok.experimental.UtilityClass;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@UtilityClass
public class DecimalFormatter {
    private static final DecimalFormat FORMATTER;

    static {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        FORMATTER = new DecimalFormat("#0.0", dfs);
    }

    public static String format(double value) {
        return FORMATTER.format(value);
    }
}

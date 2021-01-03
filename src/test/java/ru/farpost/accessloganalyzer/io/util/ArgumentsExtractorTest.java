package ru.farpost.accessloganalyzer.io.util;

import org.junit.Assert;
import org.junit.Test;
import ru.farpost.accessloganalyzer.io.Arguments;

public class ArgumentsExtractorTest {
    @Test
    public void whenValidArgumentsThenArgumentsExtracted() {
        String[] args = {"-u", "99.9", "-t", "45"};

        Arguments extractedArguments = ArgumentsExtractor.extract(args);

        Assert.assertEquals(99.9, extractedArguments.getAcceptableAvailability(), 0.01);
        Assert.assertEquals(45, extractedArguments.getAcceptableResponseTime(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNegative_U_ThenIllegalArgumentException() {
        String[] args = {"-u", "-99.9", "-t", "45"};

        ArgumentsExtractor.extract(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenTooLarge_U_ThenIllegalArgumentException() {
        String[] args = {"-u", "999.9", "-t", "45"};

        ArgumentsExtractor.extract(args);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNegative_T_ThenIllegalArgumentException() {
        String[] args = {"-u", "99.9", "-t", "-45"};

        ArgumentsExtractor.extract(args);
    }

    @Test(expected = NumberFormatException.class)
    public void when_U_isNotDoubleThen() {
        String[] args = {"-u", "test", "-t", "45"};

        ArgumentsExtractor.extract(args);
    }

    @Test(expected = NumberFormatException.class)
    public void when_T_isNotDoubleThen() {
        String[] args = {"-u", "99", "-t", "test"};

        ArgumentsExtractor.extract(args);
    }
}
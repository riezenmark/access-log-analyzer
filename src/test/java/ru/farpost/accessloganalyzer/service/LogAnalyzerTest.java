package ru.farpost.accessloganalyzer.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.farpost.accessloganalyzer.io.Arguments;

import java.io.*;

public class LogAnalyzerTest {
    private static final Arguments arguments = new Arguments();

    static {
        arguments.setAcceptableResponseTime(45);
        arguments.setAcceptableAvailability(66);
    }

    private final InputStream standardIn = System.in;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream stubOut = new ByteArrayOutputStream();

    private final LogAnalyzer analyzer = new LogAnalyzer(arguments);

    @Before
    public void setStubOutputStream() {
        System.setOut(new PrintStream(stubOut));
    }

    @After
    public void setStandardIOStreams() {
        System.setIn(standardIn);
        System.setOut(standardOut);
    }

    @Test
    public void whenAllAreAvailableThenNoOutput() {
        String log = getStubLog(
                new String[] {"200", "200", "200"},
                new String[] {"33.02583", "35.249855", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenFirstIsUnavailableThenFirstTwoAvailabilityIs50() {
        String log = getStubLog(
                new String[] {"500", "200", "200"},
                new String[] {"33.02583", "35.249855", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "01:01:01 02:02:02 50.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenSecondIsUnavailableThenSecondTwoAvailabilityIs50() {
        String log = getStubLog(
                new String[] {"200", "200", "200"},
                new String[] {"33.02583", "75.249855", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "02:02:02 03:03:03 50.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenLastIsUnavailableThenLastAvailabilityIs0() {
        String log = getStubLog(
                new String[] {"200", "200", "500"},
                new String[] {"33.02583", "15.249855", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "03:03:03 03:03:03 0.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenFirstAndSecondAreUnavailableThenAllAvailabilityIs33() {
        String log = getStubLog(
                new String[] {"500", "200", "200"},
                new String[] {"33.02583", "95.249855", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "01:01:01 03:03:03 33.3\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenSecondAndThirdAreUnavailableThenSecondTwoAvailabilityIs0() {
        String log = getStubLog(
                new String[] {"200", "200", "500"},
                new String[] {"33.02583", "95.249855", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "02:02:02 03:03:03 0.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenFirstAndThirdAreUnavailableThenAllAvailabilityIs33() {
        String log = getStubLog(
                new String[] {"200", "200", "500"},
                new String[] {"83.02583", "15.249855", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "01:01:01 03:03:03 33.3\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenAllAreUnavailableThenAllAvailabilityIs0() {
        String log = getStubLog(
                new String[] {"200", "500", "500"},
                new String[] {"83.02583", "94.249855", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "01:01:01 03:03:03 0.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenAllOfFourAreAvailableThenNoOutput() {
        String log = getStubLog(
                new String[] {"200", "200", "200", "200"},
                new String[] {"33.02583", "35.249855", "26.783072", "31.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenFirstOfFourIsUnavailableThenFirstTwoAvailabilityIs50() {
        String log = getStubLog(
                new String[] {"500", "200", "200", "200"},
                new String[] {"33.02583", "35.249855", "26.783072", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "01:01:01 02:02:02 50.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenSecondOfFourIsUnavailableThenSecondTwoAvailabilityIs50() {
        String log = getStubLog(
                new String[] {"200", "500", "200", "200"},
                new String[] {"33.02583", "35.249855", "26.783072", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "02:02:02 03:03:03 50.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenThirdOfFourIsUnavailableThenLastTwoAvailabilityIs50() {
        String log = getStubLog(
                new String[] {"200", "200", "500", "200"},
                new String[] {"33.02583", "35.249855", "26.783072", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "03:03:03 04:04:04 50.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenFourthOfFourIsUnavailableThenLastAvailabilityIs0() {
        String log = getStubLog(
                new String[] {"200", "200", "200", "500"},
                new String[] {"33.02583", "35.249855", "26.783072", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "04:04:04 04:04:04 0.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenAllOfFourAreUnavailableThenFirstTwoAvailabilityIs50() {
        String log = getStubLog(
                new String[] {"500", "500", "500", "500"},
                new String[] {"33.02583", "35.249855", "26.783072", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "01:01:01 04:04:04 0.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenFirstTwoOfFourAreUnavailableThenAllAvailabilityIs50() {
        String log = getStubLog(
                new String[] {"500", "500", "200", "200"},
                new String[] {"33.02583", "35.249855", "26.783072", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "01:01:01 04:04:04 50.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenSecondTwoOfFourAreUnavailableThenAvailabilitySinceTwoIs33() {
        String log = getStubLog(
                new String[] {"200", "500", "500", "200"},
                new String[] {"33.02583", "35.249855", "26.783072", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "02:02:02 04:04:04 33.3\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenLastTwoOfFourAreUnavailableThenLastTwoAvailabilityIs0() {
        String log = getStubLog(
                new String[] {"200", "200", "500", "500"},
                new String[] {"33.02583", "35.249855", "26.783072", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "03:03:03 04:04:04 0.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenFirstAndThirdOfFourAreUnavailableThenAllAvailabilityIs50() {
        String log = getStubLog(
                new String[] {"500", "200", "500", "200"},
                new String[] {"33.02583", "35.249855", "26.783072", "26.783072"}
        );
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "01:01:01 04:04:04 50.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenSecondAndLastOfFourAreUnavailableThenAvailabilitySinceTwoIs33() {
        String log = getStubLog(
                new String[] {"200", "500", "200", "500"},
                new String[] {"33.02583", "35.249855", "26.783072", "26.783072"}
        );
       System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "02:02:02 04:04:04 33.3\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    @Test
    public void whenFirstAndLastOfFourAreUnavailableThenFirstTwoAvailabilityIs50AndLastAvailabilityIs0() {
        String log = getStubLog(
                new String[] {"500", "200", "200", "500"},
                new String[] {"33.02583", "35.249855", "26.783072", "26.783072"}
        );

        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze();

        String expected = "01:01:01 02:02:02 50.0\n04:04:04 04:04:04 0.0\n";
        Assert.assertEquals(expected, new String(stubOut.toByteArray()));
    }

    private String getStubLog(String[] statusCodes, String[] responseTimes) {
        String result;
        if (statusCodes.length == responseTimes.length) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < statusCodes.length; i++) {
                builder.append("test - - [test")
                        .append(":0").append(i + 1)
                        .append(":0").append(i + 1)
                        .append(":0").append(i + 1)
                        .append(" +1] \"PUT test=someHash HTTP/1.1\" ")
                        .append(statusCodes[i])
                        .append(" 2 ")
                        .append(responseTimes[i])
                        .append(" \"-\" \"@test\" prio:0\n");
            }
            result = builder.toString();
        } else {
            result = "statusCodes and responseTime lengths are not equal.";
        }
        return result;
    }
}

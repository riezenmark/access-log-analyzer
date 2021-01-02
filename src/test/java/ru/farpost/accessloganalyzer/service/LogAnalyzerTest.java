package ru.farpost.accessloganalyzer.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.farpost.accessloganalyzer.io.Arguments;

import java.io.*;

public class LogAnalyzerTest {
    private static final Arguments arguments = new Arguments();

    private final InputStream standardIn = System.in;
    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream stubOut = new ByteArrayOutputStream();

    private final LogAnalyzer analyzer = new LogAnalyzer();

    static {
        arguments.setResponseTime(45);
        arguments.setAvailability(66);
    }

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
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 200 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenFirstIsUnavailableThenFirstTwoAvailabilityIs50() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 500 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "01:01:01 02:02:02 50.0\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenSecondIsUnavailableThenSecondTwoAvailabilityIs50() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 200 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 75.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "02:02:02 03:03:03 50.0\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenLastIsUnavailableThenLastAvailabilityIs0() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 200 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 15.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "03:03:03 03:03:03 0.0\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenFirstAndSecondAreUnavailableThenAllAvailabilityIs33() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 500 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 95.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "01:01:01 03:03:03 33.3\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenSecondAndThirdAreUnavailableThenSecondTwoAvailabilityIs0() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 200 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 95.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "02:02:02 03:03:03 0.0\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenFirstAndThirdAreUnavailableThenAllAvailabilityIs33() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 200 2 83.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 15.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "01:01:01 03:03:03 33.3\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenAllAreUnavailableThenAllAvailabilityIs0() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 200 2 83.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 500 2 94.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "01:01:01 03:03:03 0.0\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenAllOfFourAreAvailableThenNoOutput() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 200 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n"
              + "test - - [test:04:04:04 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 31.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenFirstOfFourIsUnavailableThenFirstTwoAvailabilityIs50() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 500 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n"
              + "test - - [test:04:04:04 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "01:01:01 02:02:02 50.0\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenSecondOfFourIsUnavailableThenSecondTwoAvailabilityIs50() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 200 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 500 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n"
              + "test - - [test:04:04:04 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "02:02:02 03:03:03 50.0\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenThirdOfFourIsUnavailableThenLastTwoAvailabilityIs50() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 200 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n"
              + "test - - [test:04:04:04 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "03:03:03 04:04:04 50.0\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenFourthOfFourIsUnavailableThenLastAvailabilityIs0() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 200 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n"
              + "test - - [test:04:04:04 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "04:04:04 04:04:04 0.0\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenAllOfFourAreUnavailableThenFirstTwoAvailabilityIs50() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 500 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 500 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n"
              + "test - - [test:04:04:04 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "01:01:01 04:04:04 0.0\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenFirstTwoOfFourAreUnavailableThenAllAvailabilityIs50() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 500 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 500 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n"
              + "test - - [test:04:04:04 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "01:01:01 04:04:04 50.0\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenSecondTwoOfFourAreUnavailableThenAvailabilitySinceTwoIs33() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 200 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 500 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n"
              + "test - - [test:04:04:04 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "02:02:02 04:04:04 33.3\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenLastTwoOfFourAreUnavailableThenLastTwoAvailabilityIs0() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 200 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n"
              + "test - - [test:04:04:04 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "03:03:03 04:04:04 0.0\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenFirstAndThirdOfFourAreUnavailableThenAllAvailabilityIs50() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 500 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n"
              + "test - - [test:04:04:04 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "01:01:01 04:04:04 50.0\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenSecondAndLastOfFourAreUnavailableThenAvailabilitySinceTwoIs33() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 200 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 500 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n"
              + "test - - [test:04:04:04 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "02:02:02 04:04:04 33.3\n",
                new String(stubOut.toByteArray())
        );
    }

    @Test
    public void whenFirstAndLastOfFourAreUnavailableThenFirstTwoAvailabilityIs50AndLastAvailabilityIs0() {
        String log =
                "test - - [test:01:01:01 +1] \"PUT test=6c21c8f6 HTTP/1.1\" 500 2 33.02583 \"-\" \"@test\" prio:0\n"
              + "test - - [test:02:02:02 +1] \"PUT test=cceed874 HTTP/1.1\" 200 2 35.249855 \"-\" \"@test\" prio:0\n"
              + "test - - [test:03:03:03 +1] \"PUT test=4b84a53c HTTP/1.1\" 200 2 26.783072 \"-\" \"@test\" prio:0\n"
              + "test - - [test:04:04:04 +1] \"PUT test=4b84a53c HTTP/1.1\" 500 2 26.783072 \"-\" \"@test\" prio:0\n";
        System.setIn(new ByteArrayInputStream(log.getBytes()));

        analyzer.analyze(arguments);

        Assert.assertEquals(
                "01:01:01 02:02:02 50.0\n"
                        + "04:04:04 04:04:04 0.0\n",
                new String(stubOut.toByteArray())
        );
    }
}

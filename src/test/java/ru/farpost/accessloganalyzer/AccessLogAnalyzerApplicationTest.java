package ru.farpost.accessloganalyzer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class AccessLogAnalyzerApplicationTest {
    private final InputStream standardInput = System.in;
    private final InputStream stubInput;

    public AccessLogAnalyzerApplicationTest() throws FileNotFoundException {
        stubInput = new FileInputStream(new File("/home/riezenmark/Рабочий стол/test.log"));
    }

    @Before
    public void setStubInput() {
        System.setIn(stubInput);
    }

    @After
    public void setStandardInput() {
        System.setIn(standardInput);
    }

    @Test
    public void test() {
        String[] args = {"-u", "99.9", "-t", "45"};
        AccessLogAnalyzerApplication.main(args);
        Assert.assertTrue(true);
    }
}

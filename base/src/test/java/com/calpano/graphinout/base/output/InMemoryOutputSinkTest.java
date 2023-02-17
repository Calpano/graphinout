package com.calpano.graphinout.base.output;

import com.calpano.graphinout.base.output.InMemoryOutputSink;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryOutputSinkTest  {

    @Test
    public void  writeTest() throws IOException {
        Random r = new Random();
        InMemoryOutputSink outputSink =  new InMemoryOutputSink();

        outputSink.outputStream().write(new String("this is for test ").getBytes());
        assertEquals("this is for test ",outputSink.readAllData().get(0));
        outputSink.outputStream().write(new String("this is for test 2 ").getBytes());
        assertEquals("this is for test this is for test 2 ",outputSink.readAllData().get(0));
        outputSink.outputStream().write(new String("\nthis is for test 3 ").getBytes());
        assertEquals("this is for test this is for test 2 \nthis is for test 3 ",outputSink.readAllData().get(0));
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.calpano.graphinout.graph.converter;

import com.calpano.graphinout.graph.GioGraphML;
import com.calpano.graphinout.xml.XMLService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author rbaba
 */
public class GioXMLServiceFactoryTest {

    public GioXMLServiceFactoryTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of instance method, of class GioXMLServiceFactory.
     */
    @Test
    public void testInstance() throws Exception {
        System.out.println("instance");
        String xmlTypeID = "DummyXMLService";

        XMLService<GioGraphML> result = GioXMLServiceFactory.instance(xmlTypeID);
        assertEquals(xmlTypeID, result.getId());

    }

    /**
     * Test of instance method, of class GioXMLServiceFactory.
     */
    @Test
    public void testInstance_not() throws Exception {
        System.out.println("instance");
        String xmlTypeID = "DummyXMLService2";

        XMLService<GioGraphML> result = GioXMLServiceFactory.instance(xmlTypeID);
        assertNull(result);

    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author RobTerpilowski
 */
public class BitmexErrorDeserializeTest {

    public BitmexErrorDeserializeTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void deserializeTest() throws Exception {
        String string = "{ \"error\": { \"message\": \"MyErrorMessage\", \"name\": \"MyErrorName\" } }";
        BitmexError expected = new BitmexError();
        BitmexErrorError inner = new BitmexErrorError();
        inner.setMessage("MyErrorMessage");
        inner.setName("MyErrorName");
        expected.setError(inner);

        ObjectMapper mapper = new ObjectMapper();
        BitmexError data = mapper.readValue(string, BitmexError.class);

        assertEquals(expected, data);
    }
}

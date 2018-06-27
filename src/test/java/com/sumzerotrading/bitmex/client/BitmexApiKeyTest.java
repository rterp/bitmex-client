/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.data.SumZeroException;
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
public class BitmexApiKeyTest {
    
    public BitmexApiKeyTest() {
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
    public void testReadApiKey() throws Exception {
        String filename = getClass().getResource("/BitmexUnitTestKey.properties").toURI().getPath();
        
        BitmexApiKey keyObject = BitmexApiKey.readApiKey(filename);
        assertEquals("MyKeyName", keyObject.getApiKeyName());
        assertEquals("MyKeyValue", keyObject.getApiKey());
    }
    
    @Test
    public void testReadApiKey_ThrowsException() {
        try {
            BitmexApiKey.readApiKey("/bogus.location");
            fail();
        } catch( SumZeroException ex ) {
            //this should happen
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author RobTerpilowski
 */
public class BitmexChartDataDeserializeTest {
    
    public BitmexChartDataDeserializeTest() {
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
        String object = "{\"timestamp\":\"2018-06-12T18:12:00.000Z\"," +
"\"symbol\":\"XBTUSD\"," +
"\"open\":6728,\"" +
"high\":6749.5," +
"\"low\":6723.5," +
"\"close\":6747.5," +
"\"trades\":120," +
"\"volume\":725177," +
"\"vwap\":6730.3809," +
"\"lastSize\":1000," +
"\"turnover\":10774807717," +
"\"homeNotional\":107.74807717,\"foreignNotional\":725177}";
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        BitmexChartData  data = mapper.readValue(object, BitmexChartData.class);
        System.out.println("data is: " + data );
        
        BitmexChartData expectdData = new BitmexChartData();
        ZonedDateTime timestamp = ZonedDateTime.of(2018, 6, 12, 18, 12, 0, 0, ZoneOffset.ofOffset("UTC", ZoneOffset.ofHours(0)));
        expectdData.setTimestamp(timestamp);
        expectdData.setSymbol("XBTUSD");
        expectdData.setOpen(6728);
        expectdData.setHigh(6749.5);
        expectdData.setLow(6723.5);
        expectdData.setClose(6747.5);
        expectdData.setTrades(120);
        expectdData.setVolume(725177);
        expectdData.setVwap(6730.3809);
        
        assertEquals( expectdData, data );
        
        
        
    }

}

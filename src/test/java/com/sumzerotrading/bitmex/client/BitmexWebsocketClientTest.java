/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.bitmex.client.JettySocket;
import com.sumzerotrading.bitmex.client.BitmexWebsocketClient;
import com.sumzerotrading.bitmex.client.WebsocketMessageProcessor;
import com.sumzerotrading.bitmex.listener.IQuoteListener;
import com.sumzerotrading.data.StockTicker;
import com.sumzerotrading.data.Ticker;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author RobTerpilowski
 */
@RunWith(MockitoJUnitRunner.class)
public class BitmexWebsocketClientTest {

    @Spy
    protected BitmexWebsocketClient testClient;

    @Mock
    protected WebsocketMessageProcessor mockMessageProcessor;

    @Mock
    protected JettySocket mockJettySocket;

    public BitmexWebsocketClientTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testClient.messageProcessor = mockMessageProcessor;
        testClient.socket = mockJettySocket;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSubscribeQuotes_notSubscribed() {
        IQuoteListener mockQuoteListener = mock(IQuoteListener.class);
        Ticker ticker = new StockTicker("ABC");
        String subscribeCommand = "SubscribeABC";
        doReturn(subscribeCommand).when(testClient).buildSubscribeCommand("quote:ABC");

        testClient.subscribeQuotes(ticker, mockQuoteListener);

        verify(mockMessageProcessor, times(1)).addQuoteListener(mockQuoteListener);
        verify(mockJettySocket, times(1)).subscribe("SubscribeABC");
    }

    @Test
    public void testSubscribeQuotes_SubscribedSameTicker() {
        IQuoteListener mockQuoteListener = mock(IQuoteListener.class);
        Ticker ticker = new StockTicker("ABC");

        String subscribeCommand = "SubscribeABC";
        doReturn(subscribeCommand).when(testClient).buildSubscribeCommand("quote:ABC");

        testClient.subscribeQuotes(ticker, mockQuoteListener);
        testClient.subscribeQuotes(ticker, mockQuoteListener);

        verify(mockMessageProcessor, times(2)).addQuoteListener(mockQuoteListener);
        verify(mockJettySocket, times(1)).subscribe("SubscribeABC");
    }

    @Test
    public void testSubscribeQuotes_SubscribedDifferentTicker() {
        IQuoteListener mockQuoteListener = mock(IQuoteListener.class);
        Ticker ticker = new StockTicker("ABC");
        Ticker tickerXYZ = new StockTicker("XYZ");
        String subscribeCommand = "SubscribeABC";
        String subscribeCommandXYZ = "SubscribeXYZ";
        doReturn(subscribeCommand).when(testClient).buildSubscribeCommand("quote:ABC");
        doReturn(subscribeCommandXYZ).when(testClient).buildSubscribeCommand("quote:XYZ");

        testClient.subscribeQuotes(ticker, mockQuoteListener);
        testClient.subscribeQuotes(tickerXYZ, mockQuoteListener);

        verify(mockMessageProcessor, times(2)).addQuoteListener(mockQuoteListener);
        verify(mockJettySocket, times(1)).subscribe("SubscribeABC");
        verify(mockJettySocket, times(1)).subscribe("SubscribeXYZ");
    }

    @Test
    public void testBuildCommandJson() {

        String expectedString = "{\"op\": \"authKey\", \"args\": [\"myApiKey\", 12345, \"myKeySig\"]}";
        String actual = testClient.buildCommandJson("authKey", "myApiKey", 12345, "myKeySig");
        System.out.println("Expected: " + expectedString);
        System.out.println("Actual: " + actual);
        assertEquals(expectedString, actual);
    }

}

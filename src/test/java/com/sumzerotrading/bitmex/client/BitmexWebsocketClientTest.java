/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.bitmex.listener.IQuoteListener;
import com.sumzerotrading.data.StockTicker;
import com.sumzerotrading.data.Ticker;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

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
    public void testConstructor_UseProduction() {
        BitmexWebsocketClient client = new BitmexWebsocketClient(true);
        assertEquals(client.productionApiUrl, client.websocketUrl);
    }

    @Test
    public void testConstructor_DontUseProduction() {
        BitmexWebsocketClient client = new BitmexWebsocketClient(false);
        assertEquals(client.testnetApiUrl, client.websocketUrl);
    }

    @Test
    public void testBuildJettySocket() {
        JettySocket socket = testClient.buildJettySocket();
        assertEquals(testClient, socket.disconnectListener);
        assertEquals(testClient.latch, socket.closeLatch);
        assertEquals(testClient.messageProcessor, socket.messageProcessor);
    }

    @Test
    public void testInit() {
        JettySocket mockJettySocket = mock(JettySocket.class);
        IMessageProcessor mockMessageProcessor = mock(IMessageProcessor.class);

        doReturn(mockJettySocket).when(testClient).buildJettySocket();
        doReturn(mockMessageProcessor).when(testClient).buildMessageProcessor();

        testClient.init();

        verify(mockMessageProcessor, times(1)).startProcessor();
        verify(mockMessageProcessor, times(1)).addPongListener(mockJettySocket);
    }

    @Test
    public void testSocketDisconnectDetected_shouldReconnect() {
        testClient.shouldReconnect = true;
        testClient.apiKey = "myKey";
        testClient.apiSecret = "mySecret";
        testClient.subscribeCommandList.add("MyNewCommand");
        doReturn(true).when(testClient).connect("myKey", "mySecret");

        testClient.socketDisconnectDetected();
        verify(testClient, times(1)).connect("myKey", "mySecret");
        verify(mockJettySocket, times(1)).subscribe("MyNewCommand");
    }

    @Test
    public void testSocketDisconnectDetected_shouldNotReconnect() {
        testClient.shouldReconnect = false;

        testClient.socketDisconnectDetected();
        verify(testClient, never()).connect(any(String.class), any(String.class));

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

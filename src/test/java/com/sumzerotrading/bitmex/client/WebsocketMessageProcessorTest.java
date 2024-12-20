/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.sumzerotrading.bitmex.entity.BitmexOrder;
import com.sumzerotrading.bitmex.entity.BitmexPosition;
import com.sumzerotrading.bitmex.entity.BitmexQuote;
import com.sumzerotrading.bitmex.entity.BitmexResponse;
import com.sumzerotrading.bitmex.entity.BitmexTrade;
import com.sumzerotrading.bitmex.listener.IOrderListener;
import com.sumzerotrading.bitmex.listener.IPongListener;
import com.sumzerotrading.bitmex.listener.IPositionListener;
import com.sumzerotrading.bitmex.listener.IQuoteListener;
import com.sumzerotrading.bitmex.listener.ITradeListener;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;


/**
 *
 * @author RobTerpilowski
 */
@RunWith(MockitoJUnitRunner.class)
@Ignore("Need to fix unit tests for changes in api since 2018")
public class WebsocketMessageProcessorTest {

    @Spy
    protected WebsocketMessageProcessor testProcessor;
    
    @Mock
    protected LinkedBlockingQueue mockQueue;
    
    //@Mock
    protected JsonParser mockParser;

    public WebsocketMessageProcessorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testProcessor.messageQueue = mockQueue;
        //testProcessor.parser = mockParser;
    }

    @After
    public void tearDown() {
    }

    
    @Test
    public void testGetSize() {
        when(mockQueue.size()).thenReturn(50);
        
        assertEquals(50, testProcessor.getQueueSize());
    }
    
    @Test
    public void testStartAndStopProcessor() throws Exception {
        doNothing().when(testProcessor).processNextMessage();

        assertFalse(testProcessor.shouldRun);
        testProcessor.startProcessor();
        Thread.sleep(100);

        assertTrue(testProcessor.shouldRun);
        testProcessor.stopProcessor();

        assertFalse(testProcessor.shouldRun);
        verify(testProcessor, atLeastOnce()).processNextMessage();
    }

    @Test
    public void testProcessMessage() {
        String message = "myMessage";
        testProcessor.processMessage(message);

        verify(mockQueue, times(1)).add(message);
    }

    @Test
    public void testAddQuoteListener() {
        IQuoteListener mockListener = mock(IQuoteListener.class);

        testProcessor.addQuoteListener(mockListener);

        assertEquals(1, testProcessor.quoteListeners.size());
        assertTrue(testProcessor.quoteListeners.contains(mockListener));
    }

    @Test
    public void testAddPositionListener() {
        IPositionListener mockListener = mock(IPositionListener.class);

        testProcessor.addPositionListener(mockListener);

        assertEquals(1, testProcessor.positionListeners.size());
        assertTrue(testProcessor.positionListeners.contains(mockListener));
    }

    @Test
    public void testAddOrderListener() {
        IOrderListener mockListener = mock(IOrderListener.class);

        testProcessor.addOrderListener(mockListener);

        assertEquals(1, testProcessor.orderListeners.size());
        assertTrue(testProcessor.orderListeners.contains(mockListener));
    }

    @Test
    public void testAddTradeListener() {
        ITradeListener mockListener = mock(ITradeListener.class);

        testProcessor.addTradeListener(mockListener);

        assertEquals(1, testProcessor.tradeListeners.size());
        assertTrue(testProcessor.tradeListeners.contains(mockListener));
    }
    
    @Test
    public void testAddPongListner() {
        IPongListener mockListener = mock(IPongListener.class);

        testProcessor.addPongListener(mockListener);

        assertEquals(1, testProcessor.pongListeners.size());
        assertTrue(testProcessor.pongListeners.contains(mockListener));
    }

    @Test
    public void testRun_shouldNotRun() {
        testProcessor.shouldRun = false;
        testProcessor.run();
        verify(testProcessor, never()).processNextMessage();
    }

    @Test
    public void testRun_shouldRun() {
        doNothing().when(testProcessor).processNextMessage();

        testProcessor.shouldRun = true;

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (Exception ex) {

            }
            testProcessor.shouldRun = false;
        });
        thread.start();

        testProcessor.run();

        verify(testProcessor, atLeastOnce()).processNextMessage();

    }
    
    @Test
    public void testProcessNextMessage_NotJsonObject() throws Exception {
        String message = "{ myVar: 123 } ";
        JsonElement element = new JsonPrimitive(Boolean.TRUE);
        when(mockQueue.take()).thenReturn(message);

        testProcessor.processNextMessage();
        
        verify(testProcessor, never()).processOrder(any(String.class));
        verify(testProcessor, never()).processPosition(any(String.class));
        verify(testProcessor, never()).processQuote(any(String.class));
        verify(testProcessor, never()).processTrade(any(String.class));
    }
    
    @Test
    public void testProcessNextMessage_PongMessage() throws Exception {
        String message = "Pong";
        JsonElement element = new JsonPrimitive(Boolean.TRUE);
        when(mockQueue.take()).thenReturn(message);

        testProcessor.processNextMessage();
        
        verify(testProcessor,times(1)).firePongReceived();
        verify(testProcessor, never()).processOrder(any(String.class));
        verify(testProcessor, never()).processPosition(any(String.class));
        verify(testProcessor, never()).processQuote(any(String.class));
        verify(testProcessor, never()).processTrade(any(String.class));
    }
    
    @Test
    public void testProcessNextMessage_JsonSyntaxException() throws Exception {
        String message = "{ masdffasdf ";
        when(mockQueue.take()).thenReturn(message);
        
        testProcessor.processNextMessage();
        
        verify(testProcessor, never()).processOrder(any(String.class));
        verify(testProcessor, never()).processPosition(any(String.class));
        verify(testProcessor, never()).processQuote(any(String.class));
        verify(testProcessor, never()).processTrade(any(String.class));        
    }
    
    @Test
    public void testProcessNextMessage_Exception() throws Exception {
        when(mockQueue.take()).thenThrow(new RuntimeException());
        
        testProcessor.processNextMessage();
        
        
        verify(testProcessor, never()).processOrder(any(String.class));
        verify(testProcessor, never()).processPosition(any(String.class));
        verify(testProcessor, never()).processQuote(any(String.class));
        verify(testProcessor, never()).processTrade(any(String.class));                
    }
    
    @Test
    public void testProcessNextMessage_NullTable() throws Exception {
        String message = "{ \"variable\": \"value\"}";
        when(mockQueue.take()).thenReturn(message);
        
        testProcessor.processNextMessage();

        verify(testProcessor, never()).processOrder(any(String.class));
        verify(testProcessor, never()).processPosition(any(String.class));
        verify(testProcessor, never()).processQuote(any(String.class));
        verify(testProcessor, never()).processTrade(any(String.class));        
        //{"table":"trade"
    }
    
    @Test
    public void testProcessNextMessage_QuoteMessage() throws Exception  {
        String message = "{\"table\":\"quote\"}";
        when(mockQueue.take()).thenReturn(message);
        
        doNothing().when(testProcessor).processOrder(any(String.class));
        doNothing().when(testProcessor).processPosition(any(String.class));
        doNothing().when(testProcessor).processQuote(any(String.class));
        doNothing().when(testProcessor).processTrade(any(String.class));
        
        testProcessor.processNextMessage();
        
        verify(testProcessor, never()).processOrder(any(String.class));
        verify(testProcessor, never()).processPosition(any(String.class));
        verify(testProcessor, times(1)).processQuote(any(String.class));
        verify(testProcessor, never()).processTrade(any(String.class));         
        
    }
    
    @Test
    public void testProcessNextMessage_PositionMessage() throws Exception  {
        String message = "{\"table\":\"position\"}";
        when(mockQueue.take()).thenReturn(message);
        
        doNothing().when(testProcessor).processOrder(any(String.class));
        doNothing().when(testProcessor).processPosition(any(String.class));
        doNothing().when(testProcessor).processQuote(any(String.class));
        doNothing().when(testProcessor).processTrade(any(String.class));
        
        testProcessor.processNextMessage();
        
        verify(testProcessor, never()).processOrder(any(String.class));
        verify(testProcessor, times(1)).processPosition(any(String.class));
        verify(testProcessor, never()).processQuote(any(String.class));
        verify(testProcessor, never()).processTrade(any(String.class));          
    }
    
    @Test
    public void testProcessNextMessage_OrderMessage() throws Exception  {
        String message = "{\"table\":\"order\"}";
        when(mockQueue.take()).thenReturn(message);
        
        doNothing().when(testProcessor).processOrder(any(String.class));
        doNothing().when(testProcessor).processPosition(any(String.class));
        doNothing().when(testProcessor).processQuote(any(String.class));
        doNothing().when(testProcessor).processTrade(any(String.class));
        
        testProcessor.processNextMessage();
        
        verify(testProcessor, times(1)).processOrder(any(String.class));
        verify(testProcessor, never()).processPosition(any(String.class));
        verify(testProcessor, never()).processQuote(any(String.class));
        verify(testProcessor, never()).processTrade(any(String.class));           
    }
    
    @Test
    public void testProcessNextMessage_TradeMessage() throws Exception  {
        String message = "{\"table\":\"trade\"}";
        when(mockQueue.take()).thenReturn(message);
        
        doNothing().when(testProcessor).processOrder(any(String.class));
        doNothing().when(testProcessor).processPosition(any(String.class));
        doNothing().when(testProcessor).processQuote(any(String.class));
        doNothing().when(testProcessor).processTrade(any(String.class));
        
        testProcessor.processNextMessage();
        
        verify(testProcessor, never()).processOrder(any(String.class));
        verify(testProcessor, never()).processPosition(any(String.class));
        verify(testProcessor, never()).processQuote(any(String.class));
        verify(testProcessor, times(1)).processTrade(any(String.class));           
    }

    
    @Test
    public void  testProcessQuote() {
        String message = "myMessage";
        BitmexResponse<BitmexQuote> response = new BitmexResponse<>();
        response.setData( new BitmexQuote[0] );
        doReturn(response).when(testProcessor).parseMessage(message, new TypeToken<BitmexResponse<BitmexQuote>>(){});
        doNothing().when(testProcessor).fireQuoteMessage(response);
        
        testProcessor.processQuote(message);
        
        verify(testProcessor, times(1)).fireQuoteMessage(response);
        
    }
    
    @Test
    public void testProcessPosition() {
        String message = "myMessage";
        BitmexResponse<BitmexPosition> response = new BitmexResponse<>();
        response.setData( new BitmexPosition[0] );
        doReturn(response).when(testProcessor).parseMessage(message, new TypeToken<BitmexResponse<BitmexPosition>>(){});
        doNothing().when(testProcessor).firePositionMessage(response);
        
        testProcessor.processPosition(message);
        
        verify(testProcessor, times(1)).firePositionMessage(response);        
    }
    
    @Test
    public void testProcessOrder() {
        String message = "myMessage";
        BitmexResponse<BitmexOrder> response = new BitmexResponse<>();
        response.setData( new BitmexOrder[0] );
        doReturn(response).when(testProcessor).parseMessage(message, new TypeToken<BitmexResponse<BitmexOrder>>(){});
        doNothing().when(testProcessor).fireOrderMessage(response);
        
        testProcessor.processOrder(message);
        
        verify(testProcessor, times(1)).fireOrderMessage(response);                
    }
    
    @Test
    public void testProcessTrade() {
        String message = "myMessage";
        BitmexResponse<BitmexTrade> response = new BitmexResponse<>();
        response.setData( new BitmexTrade[0] );
        doReturn(response).when(testProcessor).parseMessage(message, new TypeToken<BitmexResponse<BitmexTrade>>(){});
        doNothing().when(testProcessor).fireTradeMessage(response);
        
        testProcessor.processTrade(message);
        
        verify(testProcessor, times(1)).fireTradeMessage(response);                        
    }
    
    @Test
    public void testParseMessage() {
        String message = "{\"table\":\"trade\",\"action\":\"insert\",\"data\":[{\"timestamp\":\"2018-06-09T14:40:01.764Z\",\"symbol\":\"XBTUSD\",\"side\":\"Buy\",\"size\":3000,\"price\":7610.5,\"tickDirection\":\"ZeroPlusTick\",\"trdMatchID\":\"e674b065-009c-0d19-1be0-b2cf4895235e\",\"grossValue\":39420000,\"homeNotional\":0.3942,\"foreignNotional\":3000}]}";
        BitmexResponse<BitmexTrade> response =  testProcessor.parseMessage(message, new TypeToken<BitmexResponse<BitmexTrade>>(){});
        
        assertNotNull( response );
        assertEquals("trade", response.getTable() );
        BitmexTrade[] data = response.getData();
        assertEquals( 1, data.length );
    }
    
    @Test
    public void testFireQuoteMessage() {
        IQuoteListener mockQuoteListener  = mock(IQuoteListener.class);
        testProcessor.addQuoteListener(mockQuoteListener);
        
        BitmexResponse<BitmexQuote> response = new BitmexResponse<>();
        BitmexQuote[] data = new BitmexQuote[1];
        data[0] = new BitmexQuote();
        response.setData(data);
        
        testProcessor.fireQuoteMessage(response);
        
        verify(mockQuoteListener, times(1)).quoteUpdated(data[0]);
        
    }
    
    @Test
    public void testFirePositionMessage() {
        IPositionListener mockListener  = mock(IPositionListener.class);
        testProcessor.addPositionListener(mockListener);
        
        BitmexResponse<BitmexPosition> response = new BitmexResponse<>();
        BitmexPosition[] data = new BitmexPosition[1];
        data[0] = new BitmexPosition();
        response.setData(data);
        
        testProcessor.firePositionMessage(response);
        
        verify(mockListener, times(1)).positionUpdated(data[0]);        
    }
    
    @Test
    public void testFireOrderMessage() {
        IOrderListener mockListener  = mock(IOrderListener.class);
        testProcessor.addOrderListener(mockListener);
        
        BitmexResponse<BitmexOrder> response = new BitmexResponse<>();
        BitmexOrder[] data = new BitmexOrder[1];
        data[0] = new BitmexOrder();
        response.setData(data);
        
        testProcessor.fireOrderMessage(response);
        
        verify(mockListener, times(1)).orderUpdated(data[0]);           
    }
    
    @Test
    public void testFireTradeMessage() {
        ITradeListener mockListener  = mock(ITradeListener.class);
        testProcessor.addTradeListener(mockListener);
        
        BitmexResponse<BitmexTrade> response = new BitmexResponse<>();
        BitmexTrade[] data = new BitmexTrade[1];
        data[0] = new BitmexTrade();
        response.setData(data);
        
        testProcessor.fireTradeMessage(response);
        
        verify(mockListener, times(1)).tradeUpdated(data[0]);                   
    }
    
    

}



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.bitmex.listener.WebsocketDisconnectListener;
import java.util.concurrent.CountDownLatch;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 *
 * @author RobTerpilowski
 */
@RunWith(MockitoJUnitRunner.class)
public class JettySocketTest {
    
    @Spy
    protected JettySocket testJettySocket;
    
    @Mock
    protected Session mockSession;
    
    public JettySocketTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        testJettySocket.session = mockSession;
    }
    
    @After
    public void tearDown() {
    }

    
    @Test
    public void testConstructors() {
        CountDownLatch latch = new CountDownLatch(0);
        IMessageProcessor mp = mock(IMessageProcessor.class);
        WebsocketDisconnectListener wdl = mock(WebsocketDisconnectListener.class);
        JettySocket js = new JettySocket(latch, mp, wdl);
        
        assertEquals( latch, js.closeLatch);
        assertEquals( mp, js.messageProcessor);
        assertEquals( wdl, js.disconnectListener);
        
        js = new JettySocket(latch, mp);
        assertEquals( latch, js.closeLatch);
        assertEquals( mp, js.messageProcessor);
        assertNull(js.disconnectListener);
    }
    
    
    @Test
    public void testOnClose() {
        testJettySocket.shouldRun = true;
        testJettySocket.connected = true;
        WebsocketDisconnectListener mockListener = mock(WebsocketDisconnectListener.class);
        testJettySocket.disconnectListener = mockListener;
        
        testJettySocket.onClose(123, "MyReason");
        
        assertFalse( testJettySocket.shouldRun );
        assertNull( testJettySocket.session );
        assertFalse( testJettySocket.connected );
        verify( mockListener, times(1)).socketDisconnectDetected();
    }
 
    
}

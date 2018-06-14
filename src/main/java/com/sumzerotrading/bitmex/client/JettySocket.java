/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.sumzerotrading.data.SumZeroException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 *
 * @author RobTerpilowski
 */
@WebSocket(maxTextMessageSize = (64 * 1024 * 100), maxBinaryMessageSize = -1)
//@WebSocket
public class JettySocket {

    protected Logger logger = Logger.getLogger(JettySocket.class);
    protected final CountDownLatch closeLatch;
    protected boolean connected = false;
    protected volatile boolean shouldRun = true;
    protected Gson gson = new Gson();
    protected JsonParser parser = new JsonParser();
    protected IMessageProcessor messageProcessor;


    @SuppressWarnings("unused")
    private Session session;

    public JettySocket(CountDownLatch latch, IMessageProcessor messageProcessor) {
        this.closeLatch = latch;
        this.messageProcessor = messageProcessor;
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.printf("Connection closed: %d - %s%n", statusCode, reason);
        this.session = null;
        connected = false;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session = session;
        connected = true;
        closeLatch.countDown();
        startPing();
    }

    public void startPing() {
        final String pingCommand = "ping";
        Thread thread = new Thread(() -> {
            while (shouldRun) {
                try {
                    if (session != null) {
                        Future<Void> fut = session.getRemote().sendStringByFuture(pingCommand);
                        logger.info("Sending ping");
                    }
                    Thread.sleep(30000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        logger.info("msg: " + msg);
        messageProcessor.processMessage(msg);
    }

    public boolean isConnected() {
        return connected;
    }

    public void subscribe(String message) {
        logger.info("Sending command: " + message);
        Future<Void> fut = session.getRemote().sendStringByFuture(message);
        try {
            fut.get(2, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new SumZeroException(ex);
        }
    }
}

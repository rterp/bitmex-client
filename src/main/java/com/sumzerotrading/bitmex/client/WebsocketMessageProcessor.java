/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sumzerotrading.bitmex.entity.BitmexOrder;
import com.sumzerotrading.bitmex.entity.BitmexOrderResponse;
import com.sumzerotrading.bitmex.entity.BitmexPosition;
import com.sumzerotrading.bitmex.entity.BitmexPositionResponse;
import com.sumzerotrading.bitmex.entity.BitmexQuoteData;
import com.sumzerotrading.bitmex.entity.BitmexQuoteResponse;
import com.sumzerotrading.bitmex.listener.IOrderListener;
import com.sumzerotrading.bitmex.listener.IPositionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;
import com.sumzerotrading.bitmex.listener.IQuoteListener;

/**
 *
 * @author RobTerpilowski
 */
public class WebsocketMessageProcessor implements Runnable, IMessageProcessor {

    protected Logger logger = Logger.getLogger(WebsocketMessageProcessor.class);
    protected LinkedBlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    protected volatile boolean shouldRun = false;
    protected Gson gson = new Gson();
    protected JsonParser parser = new JsonParser();
    
    protected List<IQuoteListener> quoteListeners = new ArrayList<>();
    protected List<IPositionListener> positionListeners = new ArrayList<>();
    protected List<IOrderListener> orderListeners = new ArrayList<>();

    @Override
    public void startProcessor() {
        shouldRun = true;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void stopProcessor() {
        shouldRun = false;
    }

    @Override
    public void processMessage(String message) {
        messageQueue.add(message);
    }
    
    @Override
    public void addQuoteListener( IQuoteListener listener ) {
        quoteListeners.add(listener);
    }
    
    @Override
    public void addPositionListener( IPositionListener listener ) {
        positionListeners.add(listener);
    }
    
    @Override
    public void addorderListener( IOrderListener listener ) {
        orderListeners.add(listener);
    }

    @Override
    public void run() {
        String message = "";
        while (shouldRun) {
            try {
                message = messageQueue.take();
                logger.debug("Processor got message: " + message );
                JsonElement element = parser.parse(message);
                if (element.isJsonObject()) {
                    JsonElement table = element.getAsJsonObject().get("table");

                    if (table != null) {
                        String tableString = table.getAsString();
                        if (tableString.equals("quote")) {
                            processQuote(message);
                        } else if( tableString.equals("position") ) {
                            processPosition(message);
                        } else if( tableString.equals( "order" ) ) { 
                            processOrder(message);
                        }
                    }
                }
            } catch (JsonSyntaxException ex ) {
                logger.error(ex.getMessage(), ex);
                logger.error("error parsing: " + message);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }
    
    
    protected void processQuote(String message) {
        BitmexQuoteResponse response = gson.fromJson(message, BitmexQuoteResponse.class);
        logger.debug("Parsed response: " + response );
        fireQuoteMessage(response);
    }
    
    protected void processPosition(String message) { 
        BitmexPositionResponse response = gson.fromJson(message, BitmexPositionResponse.class);
        logger.debug("Parsed response: " + response );
        firePositionMessage(response);        
    }
    
    protected void processOrder(String message) { 
        BitmexOrderResponse response = gson.fromJson(message, BitmexOrderResponse.class);
        logger.debug("Parsed response: " + response );
        fireOrderMessage(response);        
    }    
    
    
    
    protected void fireQuoteMessage( BitmexQuoteResponse response ) {
        synchronized(quoteListeners) {
            for( IQuoteListener listener : quoteListeners ) {
                for( BitmexQuoteData data : response.getData() ) {
                    try {
                        listener.quoteUpdated(data);
                    } catch( Exception ex ) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        }
    }
    
    protected void firePositionMessage( BitmexPositionResponse response ) {
        synchronized(quoteListeners) {
            for( IPositionListener listener : positionListeners ) {
                for( BitmexPosition data : response.getData() ) {
                    try {
                        listener.positionUpdated(data);
                    } catch( Exception ex ) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        }
    }    
    
    protected void fireOrderMessage( BitmexOrderResponse response ) {
        synchronized(quoteListeners) {
            for( IOrderListener listener : orderListeners ) {
                for( BitmexOrder data : response.getData() ) {
                    try {
                        listener.orderUpdated(data);
                    } catch( Exception ex ) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        }
    }        
    
    
}

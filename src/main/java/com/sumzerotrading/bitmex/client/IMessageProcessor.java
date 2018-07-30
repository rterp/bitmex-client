/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.bitmex.listener.IExecutionListener;
import com.sumzerotrading.bitmex.listener.IOrderListener;
import com.sumzerotrading.bitmex.listener.IPositionListener;
import com.sumzerotrading.bitmex.listener.IQuoteListener;
import com.sumzerotrading.bitmex.listener.ITradeListener;

/**
 *
 * @author RobTerpilowski
 */
public interface IMessageProcessor {

    void addPositionListener(IPositionListener listener);

    void addQuoteListener(IQuoteListener listener);

    void addOrderListener(IOrderListener listener);
    
    void addTradeListener(ITradeListener listener);
    
    void addExecutionListener(IExecutionListener listener);

    void processMessage(String message);

    void startProcessor();

    void stopProcessor();
    
    int getQueueSize();
    
}

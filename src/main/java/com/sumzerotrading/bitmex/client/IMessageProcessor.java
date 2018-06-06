/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.bitmex.listener.IOrderListener;
import com.sumzerotrading.bitmex.listener.IPositionListener;
import com.sumzerotrading.bitmex.listener.IQuoteListener;

/**
 *
 * @author RobTerpilowski
 */
public interface IMessageProcessor {

    void addPositionListener(IPositionListener listener);

    void addQuoteListener(IQuoteListener listener);

    void addorderListener(IOrderListener listener);

    void processMessage(String message);

    void startProcessor();

    void stopProcessor();
    
}

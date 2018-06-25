/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.bitmex.entity.BitmexQuote;
import com.sumzerotrading.bitmex.listener.IOrderListener;
import com.sumzerotrading.bitmex.listener.IPositionListener;
import com.sumzerotrading.bitmex.listener.IQuoteListener;
import com.sumzerotrading.bitmex.listener.ITradeListener;
import com.sumzerotrading.data.Ticker;

/**
 *
 * @author RobTerpilowski
 */
public interface IBitmexWebsocketClient extends IQuoteListener {

    boolean connect();

    boolean connect(String apiKey, String apiSecret);

    void disconnect();

    String getApiSignature(String apiKey, long nonce);

    boolean isConnected();

    void quoteUpdated(BitmexQuote quoteData);

    void subscribeExecutions();

    void subscribeFunding(Ticker ticker);

    void subscribeInstrument(Ticker ticker);

    void subscribeOrderBook(Ticker ticker);

    void subscribeOrders(IOrderListener listener);

    void subscribePositions(IPositionListener listener);

    void subscribeQuotes(Ticker ticker, IQuoteListener listener);

    void subscribeTrades(Ticker ticker, ITradeListener listener);
    
}

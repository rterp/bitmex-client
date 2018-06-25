/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.bitmex.entity.BitmexAmendOrder;
import com.sumzerotrading.bitmex.entity.BitmexChartData;
import com.sumzerotrading.bitmex.entity.BitmexInstrument;
import com.sumzerotrading.bitmex.entity.BitmexOrder;
import com.sumzerotrading.bitmex.entity.BitmexQuote;
import com.sumzerotrading.bitmex.listener.IOrderListener;
import com.sumzerotrading.bitmex.listener.IPositionListener;
import com.sumzerotrading.bitmex.listener.IQuoteListener;
import com.sumzerotrading.bitmex.listener.ITradeListener;
import com.sumzerotrading.data.Ticker;
import java.util.List;

/**
 *
 * @author RobTerpilowski
 */
public class BitmexClient implements IBitmexClient {

    protected IBitmexRestClient restClient;
    protected IBitmexWebsocketClient websocketClient;
    
    
    public BitmexClient( boolean useProduction ) {
        restClient = new BitmexRestClient(useProduction);
        websocketClient = new BitmexWebsocketClient(useProduction);
        websocketClient.connect();
    }
    
    public BitmexClient( boolean useProduction, String apiKeyName, String apiKey ) {
        restClient = new BitmexRestClient(useProduction, apiKeyName, apiKey);
        websocketClient = new BitmexWebsocketClient(useProduction);
        websocketClient.connect(apiKeyName, apiKey);
    }
    
    
    @Override
    public BitmexOrder amendOrder(BitmexAmendOrder order) {
        return restClient.amendOrder(order);
    }

    @Override
    public BitmexOrder cancelOrder(BitmexOrder order) {
        return restClient.cancelOrder(order);
    }

    @Override
    public List<BitmexChartData> getChartData(Ticker ticker, int count, BitmexRestClient.ChartDataBinSize binSize) {
        return restClient.getChartData(ticker, count, binSize);
    }

    @Override
    public List<BitmexChartData> getChartData(Ticker ticker, int count, BitmexRestClient.ChartDataBinSize binSize, String endTime) {
        return restClient.getChartData(ticker, count, binSize, endTime);
    }

    @Override
    public List<BitmexChartData> getChartData(Ticker ticker, int count, BitmexRestClient.ChartDataBinSize binSize, String endTime, boolean getInprogressBar) {
        return restClient.getChartData(ticker, count, binSize, endTime, getInprogressBar);
    }

    
    
    @Override
    public BitmexInstrument getInstrument(Ticker ticker) {
        return restClient.getInstrument(ticker);
    }

    @Override
    public BitmexOrder submitOrder(BitmexOrder order) {
        return restClient.submitOrder(order);
    }

    @Override
    public boolean connect() {
        return websocketClient.connect();
    }

    @Override
    public boolean connect(String apiKey, String apiSecret) {
        return websocketClient.connect(apiKey, apiSecret);
    }

    @Override
    public void disconnect() {
        websocketClient.disconnect();
    }

    @Override
    public String getApiSignature(String apiKey, long nonce) {
        return websocketClient.getApiSignature(apiKey, nonce);
    }

    @Override
    public boolean isConnected() {
        return websocketClient.isConnected();
    }

    @Override
    public void quoteUpdated(BitmexQuote quoteData) {
        
    }

    @Override
    public void subscribeExecutions() {
        websocketClient.subscribeExecutions();
    }

    @Override
    public void subscribeFunding(Ticker ticker) {
        websocketClient.subscribeFunding(ticker);
    }

    @Override
    public void subscribeInstrument(Ticker ticker) {
        websocketClient.subscribeInstrument(ticker);
    }

    @Override
    public void subscribeOrderBook(Ticker ticker) {
        websocketClient.subscribeOrderBook(ticker);
    }

    @Override
    public void subscribeOrders(IOrderListener listener) {
        websocketClient.subscribeOrders(listener);
    }

    @Override
    public void subscribePositions(IPositionListener listener) {
        websocketClient.subscribePositions(listener);
    }

    @Override
    public void subscribeQuotes(Ticker ticker, IQuoteListener listener) {
        websocketClient.subscribeQuotes(ticker, listener);
    }

    @Override
    public void subscribeTrades(Ticker ticker, ITradeListener listener) {
        websocketClient.subscribeTrades(ticker, listener);
    }
    
    
    
    
}

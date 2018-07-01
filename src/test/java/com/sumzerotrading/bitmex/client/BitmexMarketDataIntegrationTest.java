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
import com.sumzerotrading.bitmex.entity.BitmexTrade;
import com.sumzerotrading.data.GenericTicker;
import com.sumzerotrading.data.StockTicker;
import com.sumzerotrading.data.Ticker;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author RobTerpilowski
 */
public class BitmexMarketDataIntegrationTest {

    public BitmexMarketDataIntegrationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    //@Test
    @Ignore
    public void testWebsockClient() throws Exception {
        BitmexWebsocketClient client = new BitmexWebsocketClient(true);
        client.connect();
        // Ticker ticker = new StockTicker("usd_btc_ticker_this_week");
        Ticker ticker = new GenericTicker("XBTUSD");
        client.subscribeTrades(ticker, (BitmexTrade trade) -> {
        });

//Thread.sleep(10000);
        //client.subscribeOrderBook(ticker);
        //client.subscribeFunding(ticker);
//        client.subscribeQuotes(ticker, (OkExQuoteData data) -> {
//            System.out.println("Data is: " + data);
//        });
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(5000000);
            } catch (InterruptedException ex) {
            }
        });
        thread.start();
        Thread.sleep(300000000);
        System.out.println("Test started");
    }

    @Ignore
    public void testRestClient() throws Exception {
        String apiKeyName = "";
        String apiKey = "";
        BitmexRestClient client = new BitmexRestClient(false, apiKeyName, apiKey);
        Ticker ticker = new StockTicker("XBTUSD");
        BitmexInstrument instrument = client.getInstrument(ticker);
        System.out.println("Instrument: " + instrument);
        System.out.println("Funding rate: " + instrument.getAnnualizedFundingRate());
        System.out.println("Indicative rate: " + instrument.getAnnualizedIndicativeFundingRate());
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        System.out.println("Next funding in: " + now.until(instrument.getFundingTimestamp(), ChronoUnit.MINUTES) + " minutes");
    }

    @Ignore
    public void testSubmitLimitOrder() throws Exception {
        String apiKeyName = "";
        String apiKey = "";
        BitmexRestClient client = new BitmexRestClient(false, apiKeyName, apiKey);
        Ticker ticker = new StockTicker("XBTUSD");
        BitmexOrder order = new BitmexOrder();
        order.setSymbol("XBTUSD");
        order.setOrderQty(1.0);
        order.setPrice(8600.0);

        BitmexOrder result = client.submitOrder(order);
        System.out.println("Order returned is: " + result);
        Thread.sleep(5000);
        BitmexAmendOrder amend = new BitmexAmendOrder();
        amend.setOrderID(result.getOrderID());
        amend.setPrice(8650.0);

        System.out.println("Submitting new order: " + amend);
        BitmexOrder newOrder = client.amendOrder(amend);
        System.out.println("New order: " + newOrder);
    }
    
    //@Ignore
    @Test
    public void testSubmitMarketOrder() throws Exception {
        String apiKeyName = "";
        String apiKey = "";
        BitmexApiKey keyProp = BitmexApiKey.readApiKey("/Users/RobTerpilowski/Documents/BitmexTestnet.prop");
        apiKeyName = keyProp.getApiKeyName();
        apiKey = keyProp.getApiKey();
        BitmexRestClient client = new BitmexRestClient(false, apiKeyName, apiKey);
        BitmexOrder order = new BitmexOrder();
        order.setSymbol("XBTUSD");
        order.setSide("Buy");
        order.setOrderQty(1.0);
        order.setOrdType("Limit");
        order.setExecInst("ParticipateDoNotInitiate");
        order.setPrice(8000.0);

        BitmexOrder result = client.submitOrder(order);
        System.out.println("Order returned is: " + result);

    }    

    @Test
    public void testGetChartData() {
        String apiKeyName = "";
        String apiKey = "";
        BitmexRestClient client = new BitmexRestClient(false, apiKeyName, apiKey);
        Ticker ticker = new StockTicker("XBTM18");
        
        List<BitmexChartData> data = client.getChartData(ticker, 100, BitmexRestClient.ChartDataBinSize.ONE_MINUTE);
        for( BitmexChartData point : data ) {
            System.out.println("Data: " + point);
        }
        
    }
    
}

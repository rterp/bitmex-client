/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.bitmex.entity.BitmexQuoteData;
import com.google.common.base.Strings;
import com.sumzerotrading.bitmex.listener.IOrderListener;
import com.sumzerotrading.bitmex.listener.IPositionListener;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import com.sumzerotrading.bitmex.listener.IQuoteListener;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author RobTerpilowski
 */
public class BitmexWebsocketClient implements IQuoteListener {

    protected String productionApiUrl = "wss://www.bitmex.com/realtime";
    protected String testnetApiUrl = "wss://testnet.bitmex.com/realtime";
    protected String websocketUrl = "";
    protected CountDownLatch latch = new CountDownLatch(1);
    protected String apiKey = "";
    protected Logger logger = Logger.getLogger(BitmexWebsocketClient.class);
    protected IMessageProcessor messageProcessor;
    protected Set<String> subscribedTickers = new HashSet<>();

    WebSocketClient client = new WebSocketClient();
    JettySocket socket;

    protected boolean isStarted = false;
    protected boolean isConnected = false;
    protected boolean subscribedPositions = false;
    protected boolean subscribedOrders = false;
    
    
    
    //for unit tests
    protected BitmexWebsocketClient() {
    }
    
    public BitmexWebsocketClient(boolean useProduction) {
        if( useProduction ) {
            websocketUrl = productionApiUrl;
        } else {
            websocketUrl = testnetApiUrl;
        }
        messageProcessor = new WebsocketMessageProcessor();
        messageProcessor.startProcessor();
        socket = new JettySocket(latch, messageProcessor);
    }

    public boolean connect() {
        return connect("", "");
    }

    public boolean connect(String apiKey, String apiSecret) {
        try {
            logger.info("Starting connection");
            client.start();
            URI echoUri = new URI(websocketUrl);
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            client.connect(socket, echoUri, request);
            logger.info("Connecting to : " + echoUri);
            latch.await(15, TimeUnit.SECONDS);
            isStarted = socket.isConnected();
            isConnected = socket.isConnected();            
            logger.info("Connected: " + isConnected );
            if (!Strings.isNullOrEmpty(apiKey)) {
                long nonce = System.currentTimeMillis();
                String signature = getApiSignature(apiSecret, nonce);
                authenticate(apiKey, nonce, signature);
            }
            //socket.startPing();
        } catch (Exception ex) {
            throw new SumZeroException(ex);
        } finally {
            return isConnected;
        }

    }

    public void subscribeExecutions() {
        socket.subscribe(buildSubscribeCommand("execution"));
    }

    public void subscribeOrders(IOrderListener listener) {
        messageProcessor.addorderListener(listener);
        if( ! subscribedOrders ) {
            socket.subscribe(buildSubscribeCommand("order"));
        } 
    }

    public void subscribePositions(IPositionListener listener) {
        messageProcessor.addPositionListener(listener);
        if( ! subscribedPositions ) {
            socket.subscribe(buildSubscribeCommand("position"));
            subscribedPositions = true;
        }
        
    }

    public void subscribeInstrument(Ticker ticker) {
        socket.subscribe(buildSubscribeCommand("instrument:" + ticker.getSymbol()));
    }

    public void subscribeFunding(Ticker ticker) {
        socket.subscribe(buildSubscribeCommand("funding:" + ticker.getSymbol()));
    }

    public void subscribeQuotes(Ticker ticker, IQuoteListener listener) {
        messageProcessor.addQuoteListener(listener);
        if( ! subscribedTickers.contains(ticker.getSymbol() ) ) {
            subscribedTickers.add(ticker.getSymbol());
            socket.subscribe(buildSubscribeCommand("quote:" + ticker.getSymbol()));
        } 
        
    }

    public void subscribeOrderBook(Ticker ticker) {
        socket.subscribe(buildSubscribeCommand("orderBookL2:" + ticker.getSymbol()));
    }

    public void subscribeTrades(Ticker ticker) {
        socket.subscribe(buildSubscribeCommand("trades:" + ticker.getSymbol()));
    }

    protected void authenticate(String apiKey, long nonce, String signature) {
        socket.subscribe(buildAuthKeyCommand(apiKey, nonce, signature));
    }

    protected String buildAuthKeyCommand(String apiKey, long nonce, String signature) {
        return buildCommandJson("authKey", apiKey, nonce, signature);
    }

    protected String buildSubscribeCommand(String... args) {
        return buildCommandJson("subscribe", args);
    }

    protected String buildCommandJson(String command, Object... args) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"op\": \"")
                .append(command)
                .append("\", \"args\": [");
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                sb.append("\"");
            }
            sb.append(args[i]);
            if (args[i] instanceof String) {
                sb.append("\"");
            }
            if (i == args.length - 1) {
                sb.append("]");
            } else {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void quoteUpdated(BitmexQuoteData quoteData) {
    }

    public String getApiSignature(String apiKey, long nonce) {

        String keyString = "GET" + "/realtime" + nonce;

        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(apiKey.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            String hash = DatatypeConverter.printHexBinary(sha256_HMAC.doFinal(keyString.getBytes()));
            return hash;
        } catch (Exception e) {
            throw new SumZeroException(e);
        }
    }

}

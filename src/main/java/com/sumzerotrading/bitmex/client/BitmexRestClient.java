/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.bitmex.entity.BitmexInstrument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.reflect.Invokable;
import com.sumzerotrading.bitmex.entity.BitmexAmendOrder;
import com.sumzerotrading.bitmex.entity.BitmexOrder;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import java.net.URI;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static javax.ws.rs.sse.SseEventSource.target;
import org.apache.log4j.Logger;

/**
 *
 * @author RobTerpilowski
 */
public class BitmexRestClient {

    protected enum Verb {
        GET, POST, DELETE, PUT
    };
    protected Logger logger = Logger.getLogger(BitmexRestClient.class);
    protected Client client = ClientBuilder.newClient();
    protected ISignatureGenerator signatureGenerator = new BitmexSignatureGenerator();
    protected String apiURL = "";
    protected String productionApiUrl = "https://www.bitmex.com/api/v1";
    protected String testnetApiUrl = "https://testnet.bitmex.com/api/v1";

    protected String apiKeyName;
    protected String apiKey;

    //Used by unit tests
    protected BitmexRestClient() {
        this(true);
    }

    public BitmexRestClient(boolean useProduction) {
        if (useProduction) {
            apiURL = productionApiUrl;
        } else {
            apiURL = testnetApiUrl;
        }
    }

    public BitmexRestClient(boolean useProduction, String apiKeyName, String apiKey) {
        this(useProduction);
        this.apiKeyName = apiKeyName;
        this.apiKey = apiKey;
    }

    public BitmexInstrument getInstrument(Ticker ticker) {
        WebTarget target = client.target(apiURL)
                .path("instrument")
                .queryParam("symbol", ticker.getSymbol());

        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);
        addHeaders(builder, target.getUri());

        Response response = builder.get();

        response.bufferEntity();
        logger.info("Response: " + response.readEntity(String.class));
        BitmexInstrument[] instruments = response.readEntity(BitmexInstrument[].class);
        return instruments[0];
    }

    public BitmexOrder submitOrder(BitmexOrder order) {
        Response response = submitRequestWithBody("order", order, Verb.POST);
        logger.info("Response code: " + response.getStatus());
        if( response.getStatus() == 503 ) {
            IllegalStateException ex = new IllegalStateException("Response code 503 returned");
            logger.error("503 response returned", ex);
        }
        return response.readEntity(BitmexOrder.class);
    }

    
    public BitmexOrder cancelOrder(BitmexOrder order) {
        Response response = submitRequestWithBody("order", order, Verb.DELETE);
        logger.info("Response code: " + response.getStatus());
        return response.readEntity(BitmexOrder.class);
    }
    
    
    public BitmexOrder amendOrder(BitmexAmendOrder order) {
        Response response = submitRequestWithBody("order", order, Verb.PUT);
        logger.info("Response code: " + response.getStatus());
        return response.readEntity(BitmexOrder.class);        
    }
    

    protected Response submitRequestWithBody(String path, Object object, Verb verb) {
        if (verb == Verb.GET) {
            throw new SumZeroException("Can't call this method for a GET request");
        }
        String jsonObject = toJson(object);
        WebTarget target = client.target(apiURL).path(path);
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);
        addHeaders(builder, target.getUri(), verb.toString(), jsonObject);
        Entity entity = Entity.json(jsonObject);
        Response response = builder.build(verb.toString(), entity).invoke();
        response.bufferEntity();
        logger.debug("Response: " + response.readEntity(String.class));
        return response;
    }

    protected String toJson(Object object) {
        ObjectMapper mapper = getObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException ex) {
            throw new SumZeroException(ex);
        }
    }

    protected void addHeaders(Invocation.Builder builder, URI uri) {
        addHeaders(builder, uri, "GET", "");
    }

    protected void addHeaders(Invocation.Builder builder, URI uri, String verb, String data) {
        if (!Strings.isNullOrEmpty(apiKey) && !Strings.isNullOrEmpty(apiKeyName)) {
            StringBuilder sb = new StringBuilder();
            sb.append(uri.getPath());
            if (!Strings.isNullOrEmpty(uri.getQuery())) {
                sb.append("?").append(uri.getQuery());
            }

            String urlPath = sb.toString();
            int expiry = getExpiry();
            logger.debug("expiry: " + expiry);
            String apiSignature = signatureGenerator.generateSignature(apiKey, verb, urlPath, expiry, data);
            builder.header("api-expires", Integer.toString(expiry))
                    .header("api-key", apiKeyName)
                    .header("api-signature", apiSignature);
            logger.debug("api-signature: " + apiSignature);
        }
    }

    protected ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    protected int getExpiry() {
        return (int) ((getSystemTime() / 1000) + 15);
    }

    protected long getSystemTime() {
        return System.currentTimeMillis();
    }

}

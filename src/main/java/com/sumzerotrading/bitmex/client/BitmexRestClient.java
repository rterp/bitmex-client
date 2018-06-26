/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.bitmex.entity.BitmexInstrument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Strings;
import com.sumzerotrading.bitmex.entity.BitmexChartData;
import com.sumzerotrading.bitmex.entity.BitmexAmendOrder;
import com.sumzerotrading.bitmex.entity.BitmexOrder;
import com.sumzerotrading.data.SumZeroException;
import com.sumzerotrading.data.Ticker;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg.Annotations;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;

/**
 *
 * @author RobTerpilowski
 */
public class BitmexRestClient implements IBitmexRestClient {

    public enum ChartDataBinSize {
        ONE_MINUTE("1m"),
        FIVE_MINUTES("5m"),
        ONE_HOUR("1h"),
        ONE_DAY("1d");

        private String bin;

        private ChartDataBinSize(String bin) {
            this.bin = bin;
        }

        public String getBin() {
            return bin;
        }

    }

    protected enum Verb {
        GET, POST, DELETE, PUT
    };
    protected Logger logger = Logger.getLogger(BitmexRestClient.class);
    protected Client client;

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
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider(mapper, new Annotations[0]);
        client = ClientBuilder.newBuilder().register(provider).register(JacksonFeature.class).build();
    }

    public BitmexRestClient(boolean useProduction, String apiKeyName, String apiKey) {
        this(useProduction);
        this.apiKeyName = apiKeyName;
        this.apiKey = apiKey;
    }

    @Override
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

    @Override
    public BitmexOrder submitOrder(BitmexOrder order) {
        Response response = submitRequestWithBody("order", order, Verb.POST);
        logger.info("Response code: " + response.getStatus());
        if (response.getStatus() == 503) {
            IllegalStateException ex = new IllegalStateException("Response code 503 returned");
            logger.error("503 response returned", ex);
        }
        return response.readEntity(BitmexOrder.class);
    }

    @Override
    public BitmexOrder cancelOrder(BitmexOrder order) {
        Response response = submitRequestWithBody("order", order, Verb.DELETE);
        logger.info("Response code: " + response.getStatus());
        return response.readEntity(BitmexOrder.class);
    }

    @Override
    public BitmexOrder amendOrder(BitmexAmendOrder order) {
        Response response = submitRequestWithBody("order", order, Verb.PUT);
        logger.info("Response code: " + response.getStatus());
        return response.readEntity(BitmexOrder.class);
    }

    @Override
    public List<BitmexChartData> getChartData(Ticker ticker, int count, ChartDataBinSize binSize) {
        return getChartData(ticker, count, binSize, "");
    }
    
    @Override
    public List<BitmexChartData> getChartData(Ticker ticker, int count, ChartDataBinSize binSize, String endTime) {
     return getChartData(ticker, count, binSize, "", false);
    }

    @Override
    public List<BitmexChartData> getChartData(Ticker ticker, int count, ChartDataBinSize binSize, String endTime, boolean getInprogressBar) {
        WebTarget target = client.target(apiURL)
                .path("trade/bucketed")
                .queryParam("symbol", ticker.getSymbol())
                .queryParam("count", count)
                .queryParam("binSize", binSize.getBin())
                .queryParam("endTime", endTime)
                .queryParam("partial", getInprogressBar)
                .queryParam("reverse", true);
        Invocation.Builder builder = target.request(MediaType.APPLICATION_JSON);
        addHeaders(builder, target.getUri());

        Response response = builder.get();

        response.bufferEntity();

        logger.info("Response: " + response.readEntity(String.class));
        BitmexChartData[] data = response.readEntity(BitmexChartData[].class);
        List<BitmexChartData> returnList = Arrays.asList(data);
        Collections.reverse(returnList);

        return returnList;   
    }
    
    

    protected Response submitRequestWithBody(String path, Object object, Verb verb) {
        if (verb == Verb.GET) {
            throw new SumZeroException("Can't call this method for a GET request");
        }
        String jsonObject = toJson(object);
        logger.debug("Submitting object: " + jsonObject);
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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.apiURL);
        hash = 97 * hash + Objects.hashCode(this.productionApiUrl);
        hash = 97 * hash + Objects.hashCode(this.testnetApiUrl);
        hash = 97 * hash + Objects.hashCode(this.apiKeyName);
        hash = 97 * hash + Objects.hashCode(this.apiKey);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BitmexRestClient other = (BitmexRestClient) obj;
        if (!Objects.equals(this.apiURL, other.apiURL)) {
            return false;
        }
        if (!Objects.equals(this.productionApiUrl, other.productionApiUrl)) {
            return false;
        }
        if (!Objects.equals(this.testnetApiUrl, other.testnetApiUrl)) {
            return false;
        }
        if (!Objects.equals(this.apiKeyName, other.apiKeyName)) {
            return false;
        }
        if (!Objects.equals(this.apiKey, other.apiKey)) {
            return false;
        }
        return true;
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.bitmex.client.BitmexRestClient;
import com.sumzerotrading.bitmex.client.ISignatureGenerator;
import com.sumzerotrading.bitmex.entity.BitmexInstrument;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sumzerotrading.bitmex.entity.BitmexAmendOrder;
import com.sumzerotrading.bitmex.entity.BitmexCancelOrder;
import com.sumzerotrading.bitmex.entity.BitmexError;
import com.sumzerotrading.bitmex.entity.BitmexOrder;
import com.sumzerotrading.data.StockTicker;
import com.sumzerotrading.data.SumZeroException;
import java.net.URI;
import java.time.ZonedDateTime;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author RobTerpilowski
 */
@RunWith(MockitoJUnitRunner.class)
public class BitmexRestClientTest {

    @Spy
    protected BitmexRestClient testClient;

    @Mock
    protected Client mockClient;

    @Mock
    protected WebTarget mockWebTarget;

    @Mock
    protected Invocation.Builder mockBuilder;

    @Mock
    protected Response mockResponse;

    @Mock
    protected ISignatureGenerator mockSignatureGenerator;

    public BitmexRestClientTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        testClient.client = mockClient;
        testClient.signatureGenerator = mockSignatureGenerator;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetInstrument() throws Exception {

        String tickerSymbol = "XBTUSD";
        StockTicker ticker = new StockTicker(tickerSymbol);
        String responseString = " [{\"symbol\":\"XBTUSD\",\"rootSymbol\":\"XBT\",\"state\":\"Open\",\"typ\":\"FFWCSX\",\"listing\":\"2016-05-04T12:00:00.000Z\",\"front\":\"2016-05-04T12:00:00.000Z\",\"expiry\":null,\"settle\":null,\"relistInterval\":null,\"inverseLeg\":\"\",\"sellLeg\":\"\",\"buyLeg\":\"\",\"optionStrikePcnt\":null,\"optionStrikeRound\":null,\"optionStrikePrice\":null,\"optionMultiplier\":null,\"positionCurrency\":\"USD\",\"underlying\":\"XBT\",\"quoteCurrency\":\"USD\",\"underlyingSymbol\":\"XBT=\",\"reference\":\"BMEX\",\"referenceSymbol\":\".BXBT\",\"calcInterval\":null,\"publishInterval\":null,\"publishTime\":null,\"maxOrderQty\":10000000,\"maxPrice\":1000000,\"lotSize\":1,\"tickSize\":0.5,\"multiplier\":-100000000,\"settlCurrency\":\"XBt\",\"underlyingToPositionMultiplier\":null,\"underlyingToSettleMultiplier\":-100000000,\"quoteToSettleMultiplier\":null,\"isQuanto\":false,\"isInverse\":true,\"initMargin\":0.01,\"maintMargin\":0.005,\"riskLimit\":20000000000,\"riskStep\":10000000000,\"limit\":null,\"capped\":false,\"taxed\":true,\"deleverage\":true,\"makerFee\":-0.00025,\"takerFee\":0.00075,\"settlementFee\":0,\"insuranceFee\":0,\"fundingBaseSymbol\":\".XBTBON8H\",\"fundingQuoteSymbol\":\".USDBON8H\",\"fundingPremiumSymbol\":\".XBTUSDPI8H\",\"fundingTimestamp\":\"2018-05-09T20:00:00.000Z\",\"fundingInterval\":\"2000-01-01T08:00:00.000Z\",\"fundingRate\":0.10,\"indicativeFundingRate\":0.00375,\"rebalanceTimestamp\":null,\"rebalanceInterval\":null,\"openingTimestamp\":\"2018-05-09T16:00:00.000Z\",\"closingTimestamp\":\"2018-05-09T18:00:00.000Z\",\"sessionInterval\":\"2000-01-01T02:00:00.000Z\",\"prevClosePrice\":9254.98,\"limitDownPrice\":null,\"limitUpPrice\":null,\"bankruptLimitDownPrice\":null,\"bankruptLimitUpPrice\":null,\"prevTotalVolume\":37216145422,\"totalVolume\":37225591592,\"volume\":9446170,\"volume24h\":281276073,\"prevTotalTurnover\":523638040493591,\"totalTurnover\":523739547136707,\"turnover\":101506643116,\"turnover24h\":3055325968796,\"prevPrice24h\":9171.5,\"vwap\":9206.4077,\"highPrice\":9450,\"lowPrice\":9000,\"lastPrice\":9324.5,\"lastPriceProtected\":9324.8788,\"lastTickDirection\":\"MinusTick\",\"lastChangePcnt\":0.0167,\"bidPrice\":9324.5,\"midPrice\":9324.75,\"askPrice\":9325,\"impactBidPrice\":9324.8788,\"impactMidPrice\":9328.25,\"impactAskPrice\":9331.8402,\"hasLiquidity\":true,\"openInterest\":135622321,\"openValue\":1454278148083,\"fairMethod\":\"FundingRate\",\"fairBasisRate\":4.10625,\"fairBasis\":10.04,\"fairPrice\":9325.99,\"markMethod\":\"FairPrice\",\"markPrice\":9325.99,\"indicativeTaxRate\":0,\"indicativeSettlePrice\":9315.95,\"optionUnderlyingPrice\":null,\"settledPrice\":null,\"timestamp\":\"2018-05-09T17:42:05.817Z\"}]";
        ZonedDateTime fundingTime = ZonedDateTime.parse("2018-05-09T20:00:00.000Z");
        BitmexInstrument[] instrumentArray = new BitmexInstrument[1];
        BitmexInstrument expectedInstrument = new BitmexInstrument();
        expectedInstrument.setFundingRate(0.1);
        expectedInstrument.setFundingTimestamp(fundingTime);
        expectedInstrument.setIndicativeFundingRate(0.00375);
        expectedInstrument.setSymbol(tickerSymbol);
        instrumentArray[0] = expectedInstrument;
        URI uri = new URI("https://www.bitmex.com/api/v1/instrument?symbol=XBTUSD");

        when(mockClient.target(testClient.productionApiUrl)).thenReturn(mockWebTarget);
        when(mockWebTarget.path("instrument")).thenReturn(mockWebTarget);
        when(mockWebTarget.queryParam("symbol", tickerSymbol)).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
        when(mockWebTarget.getUri()).thenReturn(uri);
        doNothing().when(testClient).addHeaders(mockBuilder, uri);

        when(mockBuilder.get()).thenReturn(mockResponse);
        when(mockResponse.readEntity(String.class)).thenReturn(responseString);
        when(mockResponse.readEntity(BitmexInstrument[].class)).thenReturn(instrumentArray);

        assertEquals(expectedInstrument, testClient.getInstrument(ticker));
        verify(testClient, times(1)).addHeaders(mockBuilder, uri);

    }
    
    @Test
    public void testAddHeaders_2Arg() throws Exception {
        doNothing().when(testClient).addHeaders(any(Invocation.Builder.class),any(URI.class), any(String.class), any(String.class));
        URI mockURI = new URI("http://www.foo.com");
        
        testClient.addHeaders(mockBuilder, mockURI);
        
        verify(testClient).addHeaders(mockBuilder, mockURI, "GET", "");
    }

    @Test
    public void testAddHeaders_NoApiKey_ApiKeyName() throws Exception {
        testClient.apiKeyName = "MyApiKeyName";
        URI uri = new URI("https://www.bitmex.com/api/v1/instrument?symbol=XBTUSD");

        testClient.addHeaders(mockBuilder, uri, "GET", "");

        verify(mockBuilder, never()).header(any(String.class), any(String.class));
        verify(mockSignatureGenerator, never()).generateSignature(any(String.class), any(String.class), any(String.class), any(Integer.class), any(String.class));
    }

    @Test
    public void testAddHeaders_ApiKey_NoApiKeyName() throws Exception {
        testClient.apiKey = "MyApiKey";
        URI uri = new URI("https://www.bitmex.com/api/v1/instrument?symbol=XBTUSD");

        testClient.addHeaders(mockBuilder, uri, "GET", "");

        verify(mockBuilder, never()).header(any(String.class), any(String.class));
        verify(mockSignatureGenerator, never()).generateSignature(any(String.class), any(String.class), any(String.class), any(Integer.class), any(String.class));
    }

    @Test
    public void testAddHeaders_ApiKey_ApiKeyName() throws Exception {
        String apiKey = "MyApiKey";
        String apiKeyName = "MyApiKeyName";
        String apiSignature = "MyApiSignature";
        URI uri = new URI("https://www.bitmex.com/api/v1/instrument?symbol=XBTUSD");
        int expiry = 12345;
        testClient.apiKey = apiKey;
        testClient.apiKeyName = apiKeyName;
        doReturn(expiry).when(testClient).getExpiry();
        when(mockSignatureGenerator.generateSignature(apiKey, "GET", "/api/v1/instrument?symbol=XBTUSD", expiry, "")).thenReturn(apiSignature);
        when(mockBuilder.header("api-expires", Integer.toString(expiry))).thenReturn(mockBuilder);
        when(mockBuilder.header("api-key", apiKeyName)).thenReturn(mockBuilder);
        when(mockBuilder.header("api-signature", apiSignature)).thenReturn(mockBuilder);

        testClient.addHeaders(mockBuilder, uri, "GET", "");

        verify(mockSignatureGenerator).generateSignature(apiKey, "GET", "/api/v1/instrument?symbol=XBTUSD", expiry, "");
        verify(mockBuilder).header("api-expires", Integer.toString(expiry));
        verify(mockBuilder).header("api-key", apiKeyName);
        verify(mockBuilder).header("api-signature", apiSignature);
    }
    
    @Test
    public void testAddHeaders_ApiKey_ApiKeyName_NoQueryParams() throws Exception {
        String apiKey = "MyApiKey";
        String apiKeyName = "MyApiKeyName";
        String apiSignature = "MyApiSignature";
        URI uri = new URI("https://www.bitmex.com/api/v1/instrument");
        int expiry = 12345;
        testClient.apiKey = apiKey;
        testClient.apiKeyName = apiKeyName;
        doReturn(expiry).when(testClient).getExpiry();
        when(mockSignatureGenerator.generateSignature(apiKey, "GET", "/api/v1/instrument", expiry, "")).thenReturn(apiSignature);
        when(mockBuilder.header("api-expires", Integer.toString(expiry))).thenReturn(mockBuilder);
        when(mockBuilder.header("api-key", apiKeyName)).thenReturn(mockBuilder);
        when(mockBuilder.header("api-signature", apiSignature)).thenReturn(mockBuilder);

        testClient.addHeaders(mockBuilder, uri, "GET", "");

        verify(mockSignatureGenerator).generateSignature(apiKey, "GET", "/api/v1/instrument", expiry, "");
        verify(mockBuilder).header("api-expires", Integer.toString(expiry));
        verify(mockBuilder).header("api-key", apiKeyName);
        verify(mockBuilder).header("api-signature", apiSignature);
    }    
    
    
    @Test
    public void testSubmitOrder() throws Exception {
        BitmexOrder order = new BitmexOrder();
        order.setOrderQty(2.0);
        
        BitmexOrder returnOrderObject = new BitmexOrder();

        doReturn(mockResponse).when(testClient).submitRequestWithBody("order", order, BitmexRestClient.Verb.POST);
        when(mockResponse.readEntity(BitmexOrder.class)).thenReturn(returnOrderObject);
        
        assertEquals(returnOrderObject, testClient.submitOrder(order));
    }
    
    @Test
    public void testCancelOrder() throws Exception {
        BitmexOrder order = new BitmexOrder();
        order.setOrderQty(2.0);
        order.setOrderID("MyOrderId");
        
        BitmexOrder returnOrderObject = new BitmexOrder();
        BitmexCancelOrder cancelOrder = new BitmexCancelOrder();
        cancelOrder.setOrderID("MyOrderId");

        doReturn(mockResponse).when(testClient).submitRequestWithBody("order", cancelOrder, BitmexRestClient.Verb.DELETE);
        when(mockResponse.readEntity(BitmexOrder.class)).thenReturn(returnOrderObject);
        
        assertEquals(returnOrderObject, testClient.cancelOrder(order));
    }    
    
    @Test
    public void testAmendOrder() throws Exception {
        BitmexAmendOrder order = new BitmexAmendOrder();
        order.setOrderQty(2.0);
        
        BitmexOrder returnOrderObject = new BitmexOrder();

        doReturn(mockResponse).when(testClient).submitRequestWithBody("order", order, BitmexRestClient.Verb.PUT);
        when(mockResponse.readEntity(BitmexOrder.class)).thenReturn(returnOrderObject);
        
        assertEquals(returnOrderObject, testClient.amendOrder(order));
    }        
    
    
    @Test
    public void testSubmitRequestWithBody_NotGet() throws Exception {
        String responseString = " {\"orderID\":\"f8c697c4-ca11-13f6-c601-b00cc3b600bf\",\"clOrdID\":\"\",\"clOrdLinkID\":\"\",\"account\":72539,\"symbol\":\"XBTUSD\",\"side\":\"Buy\",\"simpleOrderQty\":null,\"orderQty\":2.0,\"price\":1.55,\"displayQty\":null,\"stopPx\":null,\"pegOffsetValue\":null,\"pegPriceType\":\"\",\"currency\":\"USD\",\"settlCurrency\":\"XBt\",\"ordType\":\"Limit\",\"timeInForce\":\"GoodTillCancel\",\"execInst\":\"\",\"contingencyType\":\"\",\"exDestination\":\"XBME\",\"ordStatus\":\"New\",\"triggered\":\"\",\"workingIndicator\":true,\"ordRejReason\":\"\",\"simpleLeavesQty\":0.0001,\"leavesQty\":1,\"simpleCumQty\":0,\"cumQty\":0,\"avgPx\":null,\"multiLegReportingType\":\"SingleSecurity\",\"text\":\"Submitted via API.\",\"transactTime\":\"2018-05-10T00:07:05.873Z\",\"timestamp\":\"2018-05-10T00:07:05.873Z\"}";
        BitmexOrder order = new BitmexOrder();
        Invocation mockInvocation = mock(Invocation.class);
        doReturn("MyJsonObject").when(testClient).toJson(order);
        URI uri = new URI("https://www.bitmex.com/api/v1/order");
        when(mockClient.target(testClient.productionApiUrl)).thenReturn(mockWebTarget);
        when(mockWebTarget.path("order")).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
        when(mockWebTarget.getUri()).thenReturn(uri);
        doNothing().when(testClient).addHeaders(mockBuilder, uri, "POST", "MyJsonObject" );

        when(mockBuilder.build(eq("POST"), any(Entity.class))).thenReturn(mockInvocation);        
        when(mockInvocation.invoke()).thenReturn(mockResponse);
        when(mockResponse.readEntity(String.class)).thenReturn(responseString);
        
        assertEquals( mockResponse, testClient.submitRequestWithBody("order", order, BitmexRestClient.Verb.POST));
        verify(testClient, times(1)).addHeaders(mockBuilder, uri, "POST", "MyJsonObject");
        verify(mockResponse, times(1)).bufferEntity();
    }
    
    @Test
    public void testSubmitRequestWithBody_NotGet_ThrowsException() throws Exception {
        String responseString = "{\"error\":{\"message\":\"Too many open orders\",\"name\":\"HTTPError\"}}";
        BitmexError error = new BitmexError();
        error.setMessage(responseString);
        error.setName("MyError");
        BitmexOrder order = new BitmexOrder();
        Invocation mockInvocation = mock(Invocation.class);
        doReturn("MyJsonObject").when(testClient).toJson(order);
        URI uri = new URI("https://www.bitmex.com/api/v1/order");
        when(mockClient.target(testClient.productionApiUrl)).thenReturn(mockWebTarget);
        when(mockWebTarget.path("order")).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
        when(mockWebTarget.getUri()).thenReturn(uri);
        doNothing().when(testClient).addHeaders(mockBuilder, uri, "POST", "MyJsonObject" );

        when(mockBuilder.build(eq("POST"), any(Entity.class))).thenReturn(mockInvocation);        
        when(mockInvocation.invoke()).thenReturn(mockResponse);
        when(mockResponse.readEntity(String.class)).thenReturn(responseString);
        when(mockResponse.readEntity(BitmexError.class)).thenReturn(error);
        
        try {
            testClient.submitRequestWithBody("order", order, BitmexRestClient.Verb.POST);
            fail();
        } catch( BitmexException ex ) {
            assertEquals( error, ex.getError());
        }
        
        verify(testClient, times(1)).addHeaders(mockBuilder, uri, "POST", "MyJsonObject");
        verify(mockResponse, times(1)).bufferEntity();
    }    
    
    
    
    @Test
    public void testSubmitRequestWithBody_Get() {
        try {
            testClient.submitRequestWithBody("", this, BitmexRestClient.Verb.GET);
            fail();
        } catch( SumZeroException ex ) {
            //this should happen
        }
    }
    
    @Test
    public void testToJson() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        doReturn(mockMapper).when(testClient).getObjectMapper();
        Object object = new Object();
        
        when(mockMapper.writeValueAsString(object)).thenReturn("myobject");
        
        assertEquals("myobject", testClient.toJson(object));
        
    }
    
    @Test
    public void toJson_ThrowsException() throws Exception {
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        JsonProcessingException exception = mock(JsonProcessingException.class);
        doReturn(mockMapper).when(testClient).getObjectMapper();
        Object object = new Object();
        when(mockMapper.writeValueAsString(object)).thenThrow(exception);
        
        try {
            testClient.toJson(object);
            fail();
        } catch( SumZeroException ex ) {
            assertEquals( exception, ex.getCause());
        }
        
    }

}

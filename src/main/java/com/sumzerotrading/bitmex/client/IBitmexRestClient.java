/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.bitmex.client.BitmexRestClient.ChartDataBinSize;
import com.sumzerotrading.bitmex.entity.BitmexAmendOrder;
import com.sumzerotrading.bitmex.entity.BitmexChartData;
import com.sumzerotrading.bitmex.entity.BitmexInstrument;
import com.sumzerotrading.bitmex.entity.BitmexOrder;
import com.sumzerotrading.data.Ticker;
import java.util.List;

/**
 *
 * @author RobTerpilowski
 */
public interface IBitmexRestClient {

    BitmexOrder amendOrder(BitmexAmendOrder order);

    BitmexOrder cancelOrder(BitmexOrder order);

    List<BitmexChartData> getChartData(Ticker ticker, int count, ChartDataBinSize binSize);

    List<BitmexChartData> getChartData(Ticker ticker, int count, ChartDataBinSize binSize, String endTime);
    
    List<BitmexChartData> getChartData(Ticker ticker, int count, ChartDataBinSize binSize, String endTime, boolean getInprogressBar);

    BitmexInstrument getInstrument(Ticker ticker);

    BitmexOrder submitOrder(BitmexOrder order);
    
}

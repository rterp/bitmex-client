/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.listener;

import com.sumzerotrading.bitmex.entity.BitmexExecution;

/**
 *
 * @author RobTerpilowski
 */
public interface IExecutionListener {
    
    public void executionUpdated(BitmexExecution execution);
}

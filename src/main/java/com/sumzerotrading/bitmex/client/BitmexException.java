/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.bitmex.entity.BitmexError;

/**
 *
 * @author RobTerpilowski
 */
public class BitmexException extends RuntimeException {
    
    protected BitmexError error;

    public BitmexException(BitmexError error) {
        this.error = error;
    }

    public BitmexError getError() {
        return error;
    }
    
    
    
}

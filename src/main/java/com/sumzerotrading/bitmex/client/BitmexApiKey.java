/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sumzerotrading.bitmex.client;

import com.sumzerotrading.data.SumZeroException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author RobTerpilowski
 */
public class BitmexApiKey {

    public static String PROPKEY_API_KEY_NAME = "api.key.name";
    public static String PROPKEY_API_KEY = "api.key";

    protected String apiKeyName = "";
    protected String apiKey = "";

    public BitmexApiKey(String apiKeyName, String apiKey) {
        this.apiKeyName = apiKeyName;
        this.apiKey = apiKey;
    }

    public String getApiKeyName() {
        return apiKeyName;
    }

    public void setApiKeyName(String apiKeyName) {
        this.apiKeyName = apiKeyName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public static BitmexApiKey readApiKey(String propFile) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(propFile));
            String name  = props.getProperty(PROPKEY_API_KEY_NAME);
            String value = props.getProperty(PROPKEY_API_KEY);

            return new BitmexApiKey(name, value);
        } catch (IOException ex) {
            throw new SumZeroException(ex.getMessage(), ex);
        }

    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.apiKeyName);
        hash = 29 * hash + Objects.hashCode(this.apiKey);
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
        final BitmexApiKey other = (BitmexApiKey) obj;
        if (!Objects.equals(this.apiKeyName, other.apiKeyName)) {
            return false;
        }
        if (!Objects.equals(this.apiKey, other.apiKey)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "BitmexApiKey{" + "apiKeyName=" + apiKeyName + ", apiKey=" + apiKey + '}';
    }

}

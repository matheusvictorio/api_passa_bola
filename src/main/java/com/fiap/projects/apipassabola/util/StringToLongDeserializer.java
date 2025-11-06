package com.fiap.projects.apipassabola.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Custom deserializer to convert String to Long
 * Used for global user IDs that come as strings from frontend
 */
public class StringToLongDeserializer extends JsonDeserializer<Long> {
    
    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Long value: " + value, e);
        }
    }
}

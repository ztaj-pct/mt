package com.pct.common.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;

/**
 * @author Abhishek on 29/05/20
 */
public class InstantDeserializer extends JsonDeserializer<Instant> {
    @Override
    public Instant deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException {
        return Instant.parse(arg0.getText());
    }
}

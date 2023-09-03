package com.pct.device.serde;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * @author Abhishek on 29/05/20
 */
public class InstantSerializer extends JsonSerializer<Instant> {
    @Override
    public void serialize(Instant arg0, JsonGenerator arg1, SerializerProvider arg2) throws IOException {
        arg1.writeString(arg0.toString());
    }
}

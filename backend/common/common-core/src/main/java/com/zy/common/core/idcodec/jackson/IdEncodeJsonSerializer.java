package com.zy.common.core.idcodec.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.zy.common.core.idcodec.support.IdCodecSupport;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;

public class IdEncodeJsonSerializer extends JsonSerializer<Object> {

    private static final IdCodecSupport SUPPORT = new IdCodecSupport();

    private final boolean gtZeroOnly;

    public IdEncodeJsonSerializer(boolean gtZeroOnly) {
        this.gtZeroOnly = gtZeroOnly;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (value instanceof Collection<?> collection) {
            writeCollection(collection, gen);
            return;
        }
        if (value.getClass().isArray()) {
            writeArray(value, gen);
            return;
        }
        if (value instanceof Number number) {
            gen.writeString(SUPPORT.encodeId(number.longValue(), gtZeroOnly));
            return;
        }
        gen.writeNull();
    }

    private void writeCollection(Collection<?> values, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        for (Object value : values) {
            writeOne(value, gen);
        }
        gen.writeEndArray();
    }

    private void writeArray(Object values, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        int length = Array.getLength(values);
        for (int i = 0; i < length; i++) {
            writeOne(Array.get(values, i), gen);
        }
        gen.writeEndArray();
    }

    private void writeOne(Object value, JsonGenerator gen) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (value instanceof Number number) {
            String encoded = SUPPORT.encodeId(number.longValue(), gtZeroOnly);
            if (encoded == null) {
                gen.writeNull();
            } else {
                gen.writeString(encoded);
            }
            return;
        }
        gen.writeNull();
    }
}

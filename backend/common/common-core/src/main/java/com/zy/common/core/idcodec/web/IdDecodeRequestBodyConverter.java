package com.zy.common.core.idcodec.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zy.common.core.idcodec.annotation.IdDecode;
import com.zy.common.core.idcodec.support.IdCodecSupport;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

public class IdDecodeRequestBodyConverter implements HttpMessageConverter<Object> {

    private final ObjectMapper objectMapper;
    private final IdCodecSupport support = new IdCodecSupport();

    public IdDecodeRequestBodyConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return support.shouldDecodeClass(clazz) && supportsJson(mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Collections.singletonList(MediaType.APPLICATION_JSON);
    }

    @Override
    public Object read(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        String body = new String(inputMessage.getBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isBlank()) {
            return null;
        }
        JsonNode root = objectMapper.readTree(body);
        if (root instanceof ObjectNode objectNode) {
            decodeObjectNode(objectNode, clazz);
        }
        return objectMapper.treeToValue(root, clazz);
    }

    @Override
    public void write(Object value, MediaType contentType, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {
        throw new HttpMessageNotWritableException("IdDecodeRequestBodyConverter does not support writing");
    }

    private boolean supportsJson(MediaType mediaType) {
        return mediaType == null
                || MediaType.APPLICATION_JSON.includes(mediaType)
                || mediaType.getSubtype().endsWith("+json");
    }

    private void decodeObjectNode(ObjectNode objectNode, Class<?> type) {
        for (Field field : support.getAllFields(type)) {
            IdDecode annotation = field.getAnnotation(IdDecode.class);
            if (annotation == null || !objectNode.has(field.getName())) {
                continue;
            }
            JsonNode valueNode = objectNode.get(field.getName());
            if (valueNode == null || valueNode.isNull()) {
                continue;
            }
            if (support.isSimpleIdType(field.getType())) {
                Long decoded = support.decodeId(valueNode.asText(), field.getName());
                objectNode.put(field.getName(), decoded);
                continue;
            }
            if (support.isMap(field.getType())) {
                throw new UnsupportedOperationException("@IdDecode does not support Map fields: " + field.getName());
            }
            if (support.isArray(field.getType()) || support.isCollection(field.getType())) {
                decodeArrayField(objectNode, field, annotation, valueNode);
                continue;
            }
            if (valueNode instanceof ObjectNode childNode) {
                decodeObjectNode(childNode, field.getType());
            }
        }
    }

    private void decodeArrayField(ObjectNode objectNode, Field field, IdDecode annotation, JsonNode valueNode) {
        if (!(valueNode instanceof ArrayNode arrayNode)) {
            return;
        }
        if (annotation.type() == Object.class || support.isSimpleIdType(annotation.type())) {
            ArrayNode decodedArray = objectMapper.createArrayNode();
            for (JsonNode item : arrayNode) {
                Long decoded = support.decodeId(item.asText(), field.getName());
                if (decoded != null) {
                    decodedArray.add(decoded);
                }
            }
            objectNode.set(field.getName(), decodedArray);
            return;
        }
        if (support.isMap(annotation.type()) || support.isArray(annotation.type()) || support.isCollection(annotation.type())) {
            throw new UnsupportedOperationException("@IdDecode does not support matrix fields: " + field.getName());
        }
        for (JsonNode item : arrayNode) {
            if (item instanceof ObjectNode childNode) {
                decodeObjectNode(childNode, annotation.type());
            }
        }
    }
}

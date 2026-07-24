package com.example.fileimporter.util;

import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.MapType;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JsonObjectMapper {
    private final ObjectMapper objectMapper;
    private final MapType mapType;

    public JsonObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.mapType = objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, Object.class);
    }

    public Map<String, Object> parse(String value) {
        if (value == null || value.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            JsonNode root = objectMapper.readTree(value);
            if (root == null || !root.isObject()) {
                throw new IllegalArgumentException("Properties must be a JSON object");
            }
            return objectMapper.convertValue(root, mapType);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Properties contain malformed JSON", exception);
        }
    }

    public String pretty(Map<String, Object> value) {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }
}

package com.example.fileimporter.dto;

import com.example.fileimporter.model.Child;
import com.example.fileimporter.util.JsonObjectMapper;

import java.util.UUID;

public record ChildDetail(UUID id, String displayName, String properties, long version) {
    public static ChildDetail from(Child child, JsonObjectMapper mapper) {
        return new ChildDetail(child.getId(), child.getDisplayName(), mapper.pretty(child.getDynamicProperties()), child.getVersion());
    }
}

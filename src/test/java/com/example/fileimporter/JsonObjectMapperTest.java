package com.example.fileimporter;

import com.example.fileimporter.util.JsonObjectMapper;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JsonObjectMapperTest {
    private final JsonObjectMapper mapper = new JsonObjectMapper(new ObjectMapper());

    @Test
    void normalizesBlankAndParsesObjects() {
        assertThat(mapper.parse("  ")).isEmpty();
        assertThat(mapper.parse("{\"active\":true,\"score\":10}"))
                .containsEntry("active", true).containsEntry("score", 10);
    }

    @Test
    void rejectsMalformedAndNonObjectRoots() {
        assertThatThrownBy(() -> mapper.parse("[1,2]"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("object");
        assertThatThrownBy(() -> mapper.parse("{"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("malformed");
    }
}

package com.example.fileimporter;

import com.example.fileimporter.util.UuidGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UuidGeneratorTest {
    @Test
    void generatesVersionSevenIdentifiers() {
        assertThat(UuidGenerator.next().version()).isEqualTo(7);
    }
}

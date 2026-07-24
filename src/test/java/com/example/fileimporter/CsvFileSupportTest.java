package com.example.fileimporter;

import com.example.fileimporter.importing.CsvFileSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvFileSupportTest {
    @TempDir Path directory;
    private final CsvFileSupport support = new CsvFileSupport();

    @Test
    void acceptsBomAndCountsMultilineRecordsNotPhysicalLines() throws Exception {
        Path file = directory.resolve("input.csv");
        Files.writeString(file, "\uFEFFrecordType,parentExternalKey,parentDisplayName,childExternalKey,childDisplayName,properties\r\n"
                + "P,P-1,\"Parent\nOne\",,,,\r\n"
                + "C,P-1,,C-1,Child,\"{\"\"score\"\":10}\"\r\n");

        assertThat(support.validateAndCount(file)).isEqualTo(2);
    }

    @Test
    void rejectsWrongHeader() throws Exception {
        Path file = directory.resolve("wrong.csv");
        Files.writeString(file, "recordType,parentExternalKey\n");
        assertThatThrownBy(() -> support.validateAndCount(file)).hasMessageContaining("header");
    }
}

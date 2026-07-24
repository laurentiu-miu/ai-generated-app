package com.example.fileimporter.importing;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Component
public class CsvFileSupport {
    public static final List<String> HEADER = List.of(
            "recordType", "parentExternalKey", "parentDisplayName",
            "childExternalKey", "childDisplayName", "properties");

    public CSVFormat format() {
        return CSVFormat.RFC4180.builder()
                .setIgnoreEmptyLines(true)
                .setTrim(false)
                .get();
    }

    public long validateAndCount(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
             CSVParser parser = format().parse(reader)) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (!iterator.hasNext()) {
                throw new IllegalArgumentException("The CSV file has no header");
            }
            CSVRecord header = iterator.next();
            List<String> values = Arrays.asList(header.values());
            if (!values.isEmpty()) {
                values.set(0, stripBom(values.getFirst()));
            }
            if (!HEADER.equals(values)) {
                throw new IllegalArgumentException("The CSV header does not match the required columns");
            }
            long count = 0;
            while (iterator.hasNext()) {
                CSVRecord record = iterator.next();
                if (!isBlank(record)) count++;
            }
            return count;
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("The CSV file cannot be parsed: " + exception.getMessage(), exception);
        }
    }

    public static String stripBom(String value) {
        return value != null && value.startsWith("\uFEFF") ? value.substring(1) : value;
    }

    public static boolean isBlank(CSVRecord record) {
        return record.stream().allMatch(String::isBlank);
    }
}

package com.example.fileimporter.importing;

import java.util.List;

public record CsvRow(long lineNumber, List<String> values) {
    public String value(int index) {
        return index < values.size() ? values.get(index).trim() : "";
    }
}

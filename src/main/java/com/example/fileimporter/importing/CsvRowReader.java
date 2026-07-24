package com.example.fileimporter.importing;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.ItemStreamReader;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class CsvRowReader implements ItemStreamReader<CsvRow> {
    private final Path path;
    private final String targetType;
    private final CsvFileSupport support;
    private BufferedReader reader;
    private CSVParser parser;
    private Iterator<CSVRecord> records;

    public CsvRowReader(Path path, String targetType, CsvFileSupport support) {
        this.path = path;
        this.targetType = targetType;
        this.support = support;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
            parser = support.format().parse(reader);
            records = parser.iterator();
            if (records.hasNext()) records.next();
        } catch (Exception exception) {
            throw new ItemStreamException("Cannot open CSV file", exception);
        }
    }

    @Override
    public CsvRow read() {
        while (records.hasNext()) {
            CSVRecord record = records.next();
            if (CsvFileSupport.isBlank(record)) continue;
            String type = record.size() == 0 ? "" : CsvFileSupport.stripBom(record.get(0)).trim();
            boolean belongsHere = targetType.equals(type) || ("P".equals(targetType) && !"C".equals(type));
            if (belongsHere) return new CsvRow(parser.getCurrentLineNumber(), record.toList());
        }
        return null;
    }

    @Override
    public void close() throws ItemStreamException {
        try {
            if (parser != null) parser.close();
            if (reader != null) reader.close();
        } catch (Exception exception) {
            throw new ItemStreamException("Cannot close CSV file", exception);
        }
    }
}

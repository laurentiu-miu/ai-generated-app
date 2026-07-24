package com.example.fileimporter.importing;

import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Component
public class ImportSkipListener implements SkipListener<CsvRow, RowResult> {
}

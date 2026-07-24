package com.example.fileimporter.importing;

import com.example.fileimporter.model.FileImport;
import com.example.fileimporter.model.FileImportError;
import com.example.fileimporter.repository.ChildRepository;
import com.example.fileimporter.repository.FileImportErrorRepository;
import com.example.fileimporter.repository.FileImportRepository;
import com.example.fileimporter.repository.ParentRepository;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

import java.util.UUID;

public class ImportItemWriter implements ItemWriter<RowResult> {
    private final UUID importId;
    private final FileImportRepository importRepository;
    private final FileImportErrorRepository errorRepository;
    private final ParentRepository parentRepository;
    private final ChildRepository childRepository;

    public ImportItemWriter(UUID importId, FileImportRepository importRepository,
                            FileImportErrorRepository errorRepository, ParentRepository parentRepository,
                            ChildRepository childRepository) {
        this.importId = importId;
        this.importRepository = importRepository;
        this.errorRepository = errorRepository;
        this.parentRepository = parentRepository;
        this.childRepository = childRepository;
    }

    @Override
    public void write(Chunk<? extends RowResult> chunk) {
        FileImport fileImport = importRepository.findById(importId).orElseThrow();
        long successful = 0, failed = 0, skipped = 0;
        for (RowResult result : chunk) {
            if (result.parent() != null) parentRepository.save(result.parent());
            if (result.child() != null) childRepository.save(result.child());
            if (result.successful()) successful++;
            else {
                errorRepository.save(new FileImportError(fileImport, result.lineNumber(), result.errorCode(), result.errorMessage()));
                if (result.skipped()) skipped++; else failed++;
            }
        }
        fileImport.addResults(successful, failed, skipped);
        importRepository.save(fileImport);
    }
}

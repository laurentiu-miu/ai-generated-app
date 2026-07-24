package com.example.fileimporter.importing;

import com.example.fileimporter.model.FileImport;
import com.example.fileimporter.repository.FileImportRepository;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class ImportJobListener implements JobExecutionListener {
    private final FileImportRepository repository;

    public ImportJobListener(FileImportRepository repository) { this.repository = repository; }

    @Override
    @Transactional
    public void beforeJob(JobExecution execution) {
        require(execution).markRunning(execution.getId());
    }

    @Override
    @Transactional
    public void afterJob(JobExecution execution) {
        FileImport fileImport = require(execution);
        if (execution.getStatus() == BatchStatus.COMPLETED) {
            fileImport.markCompleted();
        } else {
            String message = execution.getAllFailureExceptions().stream()
                    .findFirst().map(Throwable::getMessage).orElse("Batch processing failed");
            fileImport.markFailed(message);
        }
    }

    private FileImport require(JobExecution execution) {
        UUID id = UUID.fromString(execution.getJobParameters().getString("importId"));
        return repository.findById(id).orElseThrow();
    }
}

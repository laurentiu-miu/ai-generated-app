package com.example.fileimporter.model;

import com.example.fileimporter.util.UuidGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_import")
public class FileImport {
    public enum Status {
        UPLOADED, QUEUED, RUNNING, COMPLETED, COMPLETED_WITH_ERRORS, FAILED;

        public boolean isFinished() {
            return this == COMPLETED || this == COMPLETED_WITH_ERRORS || this == FAILED;
        }
    }

    @Id
    private UUID id;
    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;
    @Column(name = "stored_filename", nullable = false, unique = true, length = 255)
    private String storedFilename;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status;
    @Column(name = "total_rows", nullable = false)
    private long totalRows;
    @Column(name = "processed_rows", nullable = false)
    private long processedRows;
    @Column(name = "successful_rows", nullable = false)
    private long successfulRows;
    @Column(name = "failed_rows", nullable = false)
    private long failedRows;
    @Column(name = "skipped_rows", nullable = false)
    private long skippedRows;
    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;
    @Column(name = "batch_job_execution_id")
    private Long batchJobExecutionId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "started_at")
    private Instant startedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Version
    @Column(nullable = false)
    private long version;

    protected FileImport() {
    }

    public FileImport(String originalFilename, String storedFilename, long totalRows) {
        this.id = UuidGenerator.next();
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.totalRows = totalRows;
        this.status = Status.QUEUED;
    }

    public void markRunning(long executionId) {
        status = Status.RUNNING;
        batchJobExecutionId = executionId;
        startedAt = Instant.now();
        errorMessage = null;
    }

    public void addResults(long successful, long failed, long skipped) {
        successfulRows += successful;
        failedRows += failed;
        skippedRows += skipped;
        processedRows = successfulRows + failedRows + skippedRows;
    }

    public void markCompleted() {
        status = failedRows + skippedRows > 0 ? Status.COMPLETED_WITH_ERRORS : Status.COMPLETED;
        completedAt = Instant.now();
    }

    public void markFailed(String message) {
        status = Status.FAILED;
        errorMessage = message;
        completedAt = Instant.now();
    }

    @PrePersist
    void prePersist() {
        if (id == null) id = UuidGenerator.next();
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() { updatedAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getOriginalFilename() { return originalFilename; }
    public String getStoredFilename() { return storedFilename; }
    public Status getStatus() { return status; }
    public long getTotalRows() { return totalRows; }
    public long getProcessedRows() { return processedRows; }
    public long getSuccessfulRows() { return successfulRows; }
    public long getFailedRows() { return failedRows; }
    public long getSkippedRows() { return skippedRows; }
    public String getErrorMessage() { return errorMessage; }
    public Long getBatchJobExecutionId() { return batchJobExecutionId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}

package com.example.fileimporter.model;

import com.example.fileimporter.util.UuidGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "file_import_error")
public class FileImportError {
    public enum Code {
        INVALID_COLUMN_COUNT, INVALID_RECORD_TYPE, MISSING_REQUIRED_VALUE, VALUE_TOO_LONG,
        UNEXPECTED_VALUE, INVALID_PROPERTIES, DUPLICATE_EXTERNAL_KEY, PARENT_NOT_FOUND, CHILD_PARENT_CHANGE
    }

    @Id
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_id", nullable = false)
    private FileImport fileImport;
    @Column(name = "line_number", nullable = false)
    private long lineNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "error_code", nullable = false, length = 64)
    private Code errorCode;
    @Column(nullable = false, columnDefinition = "text")
    private String message;

    protected FileImportError() {
    }

    public FileImportError(FileImport fileImport, long lineNumber, Code errorCode, String message) {
        this.id = UuidGenerator.next();
        this.fileImport = fileImport;
        this.lineNumber = lineNumber;
        this.errorCode = errorCode;
        this.message = message;
    }

    @PrePersist
    void prePersist() { if (id == null) id = UuidGenerator.next(); }

    public UUID getId() { return id; }
    public FileImport getFileImport() { return fileImport; }
    public long getLineNumber() { return lineNumber; }
    public Code getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
}

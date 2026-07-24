package com.example.fileimporter.dto;

import com.example.fileimporter.model.FileImport;

import java.util.UUID;

public record ImportProgress(UUID importId, FileImport.Status status, long totalRows, long processedRows,
                             long successfulRows, long failedRows, long skippedRows, int percentage,
                             boolean finished, String message) {
    public static ImportProgress from(FileImport value) {
        boolean finished = value.getStatus().isFinished();
        int percentage;
        if (value.getTotalRows() == 0) percentage = finished ? 100 : 0;
        else percentage = (int) Math.min(100, value.getProcessedRows() * 100 / value.getTotalRows());
        if (finished && value.getStatus() != FileImport.Status.FAILED) percentage = 100;
        String message = value.getErrorMessage() != null ? value.getErrorMessage() : switch (value.getStatus()) {
            case QUEUED, UPLOADED -> "Waiting to start";
            case RUNNING -> "Processing file";
            case COMPLETED -> "Import completed";
            case COMPLETED_WITH_ERRORS -> "Import completed with row errors";
            case FAILED -> "Import failed";
        };
        return new ImportProgress(value.getId(), value.getStatus(), value.getTotalRows(), value.getProcessedRows(),
                value.getSuccessfulRows(), value.getFailedRows(), value.getSkippedRows(), percentage, finished, message);
    }
}

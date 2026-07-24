package com.example.fileimporter.dto;

import com.example.fileimporter.model.FileImport;
import org.springframework.context.MessageSource;

import java.util.Locale;

import java.util.UUID;

public record ImportProgress(UUID importId, FileImport.Status status, String statusLabel, long totalRows, long processedRows,
                             long successfulRows, long failedRows, long skippedRows, int percentage,
                             boolean finished, String message) {
    public static ImportProgress from(FileImport value, MessageSource messageSource, Locale locale) {
        boolean finished = value.getStatus().isFinished();
        int percentage;
        if (value.getTotalRows() == 0) percentage = finished ? 100 : 0;
        else percentage = (int) Math.min(100, value.getProcessedRows() * 100 / value.getTotalRows());
        if (finished && value.getStatus() != FileImport.Status.FAILED) percentage = 100;
        String key = switch (value.getStatus()) {
            case QUEUED, UPLOADED -> "progress.waiting";
            case RUNNING -> "progress.running";
            case COMPLETED -> "progress.completed";
            case COMPLETED_WITH_ERRORS -> "progress.completedWithErrors";
            case FAILED -> "progress.failed";
        };
        String message = value.getErrorMessage() != null ? value.getErrorMessage() : messageSource.getMessage(key, null, locale);
        String statusLabel = messageSource.getMessage("status." + value.getStatus(), null, locale);
        return new ImportProgress(value.getId(), value.getStatus(), statusLabel, value.getTotalRows(), value.getProcessedRows(),
                value.getSuccessfulRows(), value.getFailedRows(), value.getSkippedRows(), percentage, finished, message);
    }
}

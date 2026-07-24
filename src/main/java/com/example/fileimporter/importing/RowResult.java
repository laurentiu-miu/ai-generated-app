package com.example.fileimporter.importing;

import com.example.fileimporter.model.Child;
import com.example.fileimporter.model.FileImportError;
import com.example.fileimporter.model.Parent;

public record RowResult(long lineNumber, Parent parent, Child child, FileImportError.Code errorCode,
                        String errorMessage, boolean skipped) {
    public static RowResult parent(long line, Parent parent) {
        return new RowResult(line, parent, null, null, null, false);
    }

    public static RowResult child(long line, Child child) {
        return new RowResult(line, null, child, null, null, false);
    }

    public static RowResult failed(long line, FileImportError.Code code, String message) {
        return new RowResult(line, null, null, code, message, false);
    }

    public static RowResult skipped(long line, String message) {
        return new RowResult(line, null, null, FileImportError.Code.DUPLICATE_EXTERNAL_KEY, message, true);
    }

    public boolean successful() { return parent != null || child != null; }
}

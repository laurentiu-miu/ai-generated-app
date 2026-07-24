package com.example.fileimporter.importing;

import com.example.fileimporter.model.Child;
import com.example.fileimporter.model.FileImportError;
import com.example.fileimporter.model.Parent;
import com.example.fileimporter.repository.ChildRepository;
import com.example.fileimporter.repository.ParentRepository;
import com.example.fileimporter.util.JsonObjectMapper;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CsvRowProcessor implements ItemProcessor<CsvRow, RowResult> {
    private final String targetType;
    private final ParentRepository parentRepository;
    private final ChildRepository childRepository;
    private final JsonObjectMapper jsonMapper;
    private final Set<String> seen = new HashSet<>();

    public CsvRowProcessor(String targetType, ParentRepository parentRepository,
                           ChildRepository childRepository, JsonObjectMapper jsonMapper) {
        this.targetType = targetType;
        this.parentRepository = parentRepository;
        this.childRepository = childRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public RowResult process(CsvRow row) {
        if (row.values().size() != 6) {
            return failed(row, FileImportError.Code.INVALID_COLUMN_COUNT, "Expected exactly 6 columns");
        }
        String recordType = row.value(0);
        if (!recordType.equals("P") && !recordType.equals("C")) {
            return failed(row, FileImportError.Code.INVALID_RECORD_TYPE, "recordType must be P or C");
        }
        return "P".equals(targetType) ? processParent(row) : processChild(row);
    }

    private RowResult processParent(CsvRow row) {
        String key = row.value(1);
        if (!key.isEmpty() && !seen.add(key)) return RowResult.skipped(row.lineNumber(), "Duplicate Parent external key: " + key);
        if (key.isEmpty() || row.value(2).isEmpty()) return failed(row, FileImportError.Code.MISSING_REQUIRED_VALUE, "Parent key and display name are required");
        if (tooLong(key, row.value(2))) return failed(row, FileImportError.Code.VALUE_TOO_LONG, "Parent values must contain at most 255 characters");
        if (!row.value(3).isEmpty() || !row.value(4).isEmpty()) return failed(row, FileImportError.Code.UNEXPECTED_VALUE, "Parent rows cannot contain Child values");
        Map<String, Object> properties = properties(row);
        if (properties == null) return failed(row, FileImportError.Code.INVALID_PROPERTIES, "properties must be a JSON object");
        Parent parent = parentRepository.findByExternalKey(key)
                .orElseGet(() -> Parent.fromImport(key, row.value(2), properties));
        parent.updateFromImport(key, row.value(2), properties);
        return RowResult.parent(row.lineNumber(), parent);
    }

    private RowResult processChild(CsvRow row) {
        String childKey = row.value(3);
        if (!childKey.isEmpty() && !seen.add(childKey)) return RowResult.skipped(row.lineNumber(), "Duplicate Child external key: " + childKey);
        if (row.value(1).isEmpty() || childKey.isEmpty() || row.value(4).isEmpty()) return failed(row, FileImportError.Code.MISSING_REQUIRED_VALUE, "Parent key, Child key and Child display name are required");
        if (tooLong(row.value(1), childKey, row.value(4))) return failed(row, FileImportError.Code.VALUE_TOO_LONG, "Child values must contain at most 255 characters");
        if (!row.value(2).isEmpty()) return failed(row, FileImportError.Code.UNEXPECTED_VALUE, "Child rows cannot contain a Parent display name");
        Parent parent = parentRepository.findByExternalKey(row.value(1)).orElse(null);
        if (parent == null) return failed(row, FileImportError.Code.PARENT_NOT_FOUND, "Referenced Parent was not found");
        Map<String, Object> properties = properties(row);
        if (properties == null) return failed(row, FileImportError.Code.INVALID_PROPERTIES, "properties must be a JSON object");
        Child child = childRepository.findByExternalKey(childKey).orElse(null);
        if (child != null && !child.getParent().getId().equals(parent.getId())) {
            return failed(row, FileImportError.Code.CHILD_PARENT_CHANGE, "An existing Child cannot move to another Parent");
        }
        if (child == null) child = Child.fromImport(parent, childKey, row.value(4), properties);
        child.updateFromImport(childKey, row.value(4), properties);
        return RowResult.child(row.lineNumber(), child);
    }

    private Map<String, Object> properties(CsvRow row) {
        try { return jsonMapper.parse(row.value(5)); }
        catch (IllegalArgumentException exception) { return null; }
    }

    private boolean tooLong(String... values) {
        for (String value : values) if (value.length() > 255) return true;
        return false;
    }

    private RowResult failed(CsvRow row, FileImportError.Code code, String message) {
        return RowResult.failed(row.lineNumber(), code, message);
    }
}

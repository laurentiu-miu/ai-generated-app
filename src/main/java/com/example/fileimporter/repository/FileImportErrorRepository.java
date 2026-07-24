package com.example.fileimporter.repository;

import com.example.fileimporter.model.FileImportError;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface FileImportErrorRepository extends JpaRepository<FileImportError, UUID> {
    List<FileImportError> findByFileImportIdOrderByLineNumberAscIdAsc(UUID fileImportId);
}

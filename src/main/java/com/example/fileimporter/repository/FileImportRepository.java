package com.example.fileimporter.repository;

import com.example.fileimporter.model.FileImport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileImportRepository extends JpaRepository<FileImport, java.util.UUID> {
    List<FileImport> findAllByOrderByCreatedAtDescIdDesc();
}

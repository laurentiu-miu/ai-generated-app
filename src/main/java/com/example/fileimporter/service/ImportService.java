package com.example.fileimporter.service;

import com.example.fileimporter.exception.ResourceNotFoundException;
import com.example.fileimporter.importing.SecureUploadStorage;
import com.example.fileimporter.model.FileImport;
import com.example.fileimporter.model.FileImportError;
import com.example.fileimporter.repository.FileImportErrorRepository;
import com.example.fileimporter.repository.FileImportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class ImportService {
    private final FileImportRepository repository;
    private final FileImportErrorRepository errorRepository;
    private final SecureUploadStorage storage;
    private final ImportJobLauncher jobLauncher;

    public ImportService(FileImportRepository repository, FileImportErrorRepository errorRepository,
                         SecureUploadStorage storage, ImportJobLauncher jobLauncher) {
        this.repository = repository;
        this.errorRepository = errorRepository;
        this.storage = storage;
        this.jobLauncher = jobLauncher;
    }

    @Transactional(readOnly = true)
    public List<FileImport> findAll() { return repository.findAllByOrderByCreatedAtDescIdDesc(); }

    @Transactional(readOnly = true)
    public FileImport require(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Import not found"));
    }

    @Transactional(readOnly = true)
    public List<FileImportError> errors(UUID id) {
        require(id);
        return errorRepository.findByFileImportIdOrderByLineNumberAscIdAsc(id);
    }

    public FileImport submit(MultipartFile multipartFile) {
        SecureUploadStorage.StoredUpload upload = storage.store(multipartFile);
        FileImport fileImport = persistQueued(upload);
        try {
            jobLauncher.launch(fileImport.getId(), upload.path());
            return fileImport;
        } catch (Exception exception) {
            markFailed(fileImport.getId(), "The import could not be queued: " + exception.getMessage());
            return require(fileImport.getId());
        }
    }

    FileImport persistQueued(SecureUploadStorage.StoredUpload upload) {
        return repository.save(new FileImport(upload.originalFilename(), upload.storedFilename(), upload.totalRows()));
    }

    public void markFailed(UUID id, String message) {
        FileImport fileImport = require(id);
        fileImport.markFailed(message);
        repository.save(fileImport);
    }

    public Path pathFor(FileImport fileImport) { return storage.resolve(fileImport.getStoredFilename()); }
}

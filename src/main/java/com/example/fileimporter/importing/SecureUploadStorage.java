package com.example.fileimporter.importing;

import com.example.fileimporter.config.UploadProperties;
import com.example.fileimporter.util.UuidGenerator;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Locale;

@Component
public class SecureUploadStorage {
    private final UploadProperties properties;
    private final CsvFileSupport csvFileSupport;
    private Path root;

    public SecureUploadStorage(UploadProperties properties, CsvFileSupport csvFileSupport) {
        this.properties = properties;
        this.csvFileSupport = csvFileSupport;
    }

    @PostConstruct
    void initialize() throws IOException {
        Path configured = properties.directory().toAbsolutePath().normalize();
        rejectExistingSymlinks(configured);
        Files.createDirectories(configured);
        if (Files.isSymbolicLink(configured)) {
            throw new IllegalStateException("Upload directory cannot be a symbolic link");
        }
        root = configured.toRealPath(LinkOption.NOFOLLOW_LINKS);
    }

    private void rejectExistingSymlinks(Path path) {
        Path current = path.getRoot();
        for (Path component : path) {
            current = current.resolve(component);
            if (Files.exists(current, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(current)) {
                throw new IllegalStateException("Upload directory cannot contain symbolic links");
            }
        }
    }

    public StoredUpload store(MultipartFile file) {
        validateRequest(file);
        String original = basename(file.getOriginalFilename());
        String stored = UuidGenerator.next() + ".csv";
        Path target = root.resolve(stored).normalize();
        if (!target.getParent().equals(root)) {
            throw new IllegalArgumentException("Invalid storage path");
        }
        try {
            Files.copy(file.getInputStream(), target);
            if (Files.isSymbolicLink(target) || !target.toRealPath(LinkOption.NOFOLLOW_LINKS).getParent().equals(root)) {
                throw new IllegalArgumentException("Upload path escaped the configured directory");
            }
            long totalRows = csvFileSupport.validateAndCount(target);
            return new StoredUpload(original, stored, target, totalRows);
        } catch (Exception exception) {
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {
                exception.addSuppressed(ignored);
            }
            if (exception instanceof IllegalArgumentException invalid) throw invalid;
            throw new IllegalArgumentException("The upload could not be stored", exception);
        }
    }

    public Path resolve(String storedFilename) {
        Path path = root.resolve(storedFilename).normalize();
        if (!path.getParent().equals(root)) throw new IllegalArgumentException("Invalid stored filename");
        return path;
    }

    private void validateRequest(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Select a non-empty CSV file");
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank()) throw new IllegalArgumentException("The original filename is required");
        String basename = basename(original);
        if (!basename.toLowerCase(Locale.ROOT).endsWith(".csv")) throw new IllegalArgumentException("Only .csv files are accepted");
        if (basename.length() > 255) throw new IllegalArgumentException("The original filename is too long");
        if (file.getSize() > properties.maxSize().toBytes()) throw new IllegalArgumentException("The CSV file exceeds the configured size limit");
    }

    private String basename(String value) {
        String normalized = value.replace('\\', '/');
        return normalized.substring(normalized.lastIndexOf('/') + 1);
    }

    public record StoredUpload(String originalFilename, String storedFilename, Path path, long totalRows) {
    }
}

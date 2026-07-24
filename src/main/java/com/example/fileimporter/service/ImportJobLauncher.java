package com.example.fileimporter.service;

import java.nio.file.Path;
import java.util.UUID;

public interface ImportJobLauncher {
    void launch(UUID importId, Path filePath) throws Exception;
}

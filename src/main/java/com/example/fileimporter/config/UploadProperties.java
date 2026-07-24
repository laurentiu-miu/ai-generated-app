package com.example.fileimporter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "app.upload")
public record UploadProperties(Path directory, DataSize maxSize) {
}

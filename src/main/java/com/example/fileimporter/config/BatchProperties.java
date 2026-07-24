package com.example.fileimporter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.batch")
public record BatchProperties(int chunkSize, int corePoolSize, int maxPoolSize, int queueCapacity) {
}

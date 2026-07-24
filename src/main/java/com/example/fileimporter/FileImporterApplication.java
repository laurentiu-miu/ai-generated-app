package com.example.fileimporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FileImporterApplication {
    public static void main(String[] args) {
        SpringApplication.run(FileImporterApplication.class, args);
    }
}

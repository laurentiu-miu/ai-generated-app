package com.example.fileimporter.controller;

import com.example.fileimporter.dto.ImportProgress;
import com.example.fileimporter.dto.ApiError;
import com.example.fileimporter.exception.ResourceNotFoundException;
import com.example.fileimporter.service.ImportService;
import org.springframework.http.HttpStatus;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@RestController
@RequestMapping("/api/imports")
public class ImportProgressController {
    private final ImportService importService;
    private final MessageSource messageSource;

    public ImportProgressController(ImportService importService, MessageSource messageSource) {
        this.importService = importService;
        this.messageSource = messageSource;
    }

    @GetMapping("/{importId}/progress")
    public ImportProgress progress(@PathVariable UUID importId) {
        return ImportProgress.from(importService.require(importId), messageSource, LocaleContextHolder.getLocale());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound() {
        return new ApiError(404, message("error.notFound.title"), message("error.notFound.message"));
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
}

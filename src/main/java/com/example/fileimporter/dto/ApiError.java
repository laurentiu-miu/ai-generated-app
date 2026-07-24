package com.example.fileimporter.dto;

public record ApiError(int status, String title, String detail) {
}

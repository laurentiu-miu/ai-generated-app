package com.example.fileimporter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ParentForm {
    @NotBlank(message = "Display name is required")
    @Size(max = 255, message = "Display name must contain at most 255 characters")
    private String displayName = "";
    private String dynamicProperties = "{}";
    private Long version;

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDynamicProperties() { return dynamicProperties; }
    public void setDynamicProperties(String dynamicProperties) { this.dynamicProperties = dynamicProperties; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}

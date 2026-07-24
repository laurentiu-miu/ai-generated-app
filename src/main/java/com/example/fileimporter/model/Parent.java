package com.example.fileimporter.model;

import com.example.fileimporter.util.UuidGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "parent")
public class Parent {
    @Id
    private UUID id;

    @Column(name = "external_key", length = 255, unique = true)
    private String externalKey;

    @Column(name = "display_name", nullable = false, length = 255)
    private String displayName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dynamic_properties", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> dynamicProperties = new LinkedHashMap<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Parent() {
    }

    public Parent(String displayName, Map<String, Object> dynamicProperties) {
        this.id = UuidGenerator.next();
        update(displayName, dynamicProperties);
    }

    public static Parent fromImport(String externalKey, String displayName, Map<String, Object> properties) {
        Parent parent = new Parent(displayName, properties);
        parent.externalKey = externalKey;
        return parent;
    }

    public void update(String displayName, Map<String, Object> dynamicProperties) {
        this.displayName = displayName.trim();
        this.dynamicProperties = new LinkedHashMap<>(dynamicProperties);
    }

    public void updateFromImport(String externalKey, String displayName, Map<String, Object> dynamicProperties) {
        this.externalKey = externalKey;
        update(displayName, dynamicProperties);
    }

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UuidGenerator.next();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getExternalKey() { return externalKey; }
    public String getDisplayName() { return displayName; }
    public Map<String, Object> getDynamicProperties() { return dynamicProperties; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}

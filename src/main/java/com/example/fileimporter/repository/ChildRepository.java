package com.example.fileimporter.repository;

import com.example.fileimporter.model.Child;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChildRepository extends JpaRepository<Child, UUID> {
    List<Child> findByParentIdOrderByDisplayNameAscIdAsc(UUID parentId);
    Optional<Child> findByIdAndParentId(UUID id, UUID parentId);
    Optional<Child> findByExternalKey(String externalKey);
    boolean existsByParentId(UUID parentId);
}

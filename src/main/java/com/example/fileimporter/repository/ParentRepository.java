package com.example.fileimporter.repository;

import com.example.fileimporter.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParentRepository extends JpaRepository<Parent, UUID> {
    List<Parent> findAllByOrderByDisplayNameAscIdAsc();
    Optional<Parent> findByExternalKey(String externalKey);
}

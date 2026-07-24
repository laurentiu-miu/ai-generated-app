package com.example.fileimporter.service;

import com.example.fileimporter.exception.ConflictException;
import com.example.fileimporter.exception.ResourceNotFoundException;
import com.example.fileimporter.model.Parent;
import com.example.fileimporter.repository.ChildRepository;
import com.example.fileimporter.repository.ParentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ParentService {
    private final ParentRepository parentRepository;
    private final ChildRepository childRepository;

    public ParentService(ParentRepository parentRepository, ChildRepository childRepository) {
        this.parentRepository = parentRepository;
        this.childRepository = childRepository;
    }

    @Transactional(readOnly = true)
    public List<Parent> findAll() {
        return parentRepository.findAllByOrderByDisplayNameAscIdAsc();
    }

    @Transactional(readOnly = true)
    public Parent require(UUID id) {
        return parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
    }

    @Transactional
    public Parent create(String displayName, Map<String, Object> properties) {
        return parentRepository.save(new Parent(displayName, properties));
    }

    @Transactional
    public Parent update(UUID id, long submittedVersion, String displayName, Map<String, Object> properties) {
        Parent parent = require(id);
        verifyVersion(parent.getVersion(), submittedVersion);
        parent.update(displayName, properties);
        return parent;
    }

    @Transactional
    public void delete(UUID id, long submittedVersion) {
        Parent parent = require(id);
        verifyVersion(parent.getVersion(), submittedVersion);
        if (childRepository.existsByParentId(id)) {
            throw new ConflictException("A parent with children cannot be deleted");
        }
        parentRepository.delete(parent);
        parentRepository.flush();
    }

    private void verifyVersion(long currentVersion, long submittedVersion) {
        if (currentVersion != submittedVersion) {
            throw new ConflictException("This parent was changed by another request");
        }
    }
}

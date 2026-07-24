package com.example.fileimporter.service;

import com.example.fileimporter.exception.ConflictException;
import com.example.fileimporter.exception.ResourceNotFoundException;
import com.example.fileimporter.model.Child;
import com.example.fileimporter.model.Parent;
import com.example.fileimporter.repository.ChildRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ChildService {
    private final ChildRepository childRepository;
    private final ParentService parentService;

    public ChildService(ChildRepository childRepository, ParentService parentService) {
        this.childRepository = childRepository;
        this.parentService = parentService;
    }

    @Transactional(readOnly = true)
    public List<Child> findForParent(UUID parentId) {
        parentService.require(parentId);
        return childRepository.findByParentIdOrderByDisplayNameAscIdAsc(parentId);
    }

    @Transactional(readOnly = true)
    public Child require(UUID parentId, UUID childId) {
        return childRepository.findByIdAndParentId(childId, parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Child not found under this parent"));
    }

    @Transactional
    public Child create(UUID parentId, String displayName, Map<String, Object> properties) {
        Parent parent = parentService.require(parentId);
        return childRepository.save(new Child(parent, displayName, properties));
    }

    @Transactional
    public Child update(UUID parentId, UUID childId, long submittedVersion,
                        String displayName, Map<String, Object> properties) {
        Child child = require(parentId, childId);
        verifyVersion(child.getVersion(), submittedVersion);
        child.update(displayName, properties);
        return child;
    }

    @Transactional
    public void delete(UUID parentId, UUID childId, long submittedVersion) {
        Child child = require(parentId, childId);
        verifyVersion(child.getVersion(), submittedVersion);
        childRepository.delete(child);
        childRepository.flush();
    }

    private void verifyVersion(long currentVersion, long submittedVersion) {
        if (currentVersion != submittedVersion) {
            throw new ConflictException("This child was changed by another request");
        }
    }
}

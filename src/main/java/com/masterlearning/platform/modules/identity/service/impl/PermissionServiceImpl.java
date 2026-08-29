package com.masterlearning.platform.modules.identity.service.impl;

import com.masterlearning.platform.modules.identity.dto.response.PermissionResponse;
import com.masterlearning.platform.modules.identity.mapper.PermissionMapper;
import com.masterlearning.platform.modules.identity.repository.PermissionRepository;
import com.masterlearning.platform.modules.identity.service.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(
            PermissionRepository permissionRepository,
            PermissionMapper permissionMapper
    ) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll()
                .stream()
                .map(permissionMapper::toResponse)
                .toList();
    }
}
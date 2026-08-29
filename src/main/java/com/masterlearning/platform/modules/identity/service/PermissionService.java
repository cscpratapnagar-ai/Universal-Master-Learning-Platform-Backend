package com.masterlearning.platform.modules.identity.service;

import com.masterlearning.platform.modules.identity.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {

    List<PermissionResponse> getAll();
}
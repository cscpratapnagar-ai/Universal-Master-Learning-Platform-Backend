package com.masterlearning.platform.modules.identity.service;

import com.masterlearning.platform.modules.identity.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {

    List<RoleResponse> getAll();
}
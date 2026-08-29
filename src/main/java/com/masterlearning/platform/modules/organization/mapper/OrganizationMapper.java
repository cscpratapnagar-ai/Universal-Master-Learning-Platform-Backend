package com.masterlearning.platform.modules.organization.mapper;
import com.masterlearning.platform.modules.organization.dto.response.OrganizationResponse;
import com.masterlearning.platform.modules.organization.entity.Organization;
import org.mapstruct.Mapper;
@Mapper(componentModel="spring")
public interface OrganizationMapper { OrganizationResponse toResponse(Organization organization); }
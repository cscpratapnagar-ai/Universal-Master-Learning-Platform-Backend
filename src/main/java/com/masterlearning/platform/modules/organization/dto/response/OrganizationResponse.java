package com.masterlearning.platform.modules.organization.dto.response;
import java.util.UUID;
public record OrganizationResponse(UUID id,String code,String name,String description,boolean active){}
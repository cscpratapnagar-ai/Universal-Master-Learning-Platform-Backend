package com.masterlearning.platform.modules.organization.dto.response;
import java.util.UUID;
import java.util.List;
public record OrganizationMemberResponse(UUID id,UUID userId,String email,String firstName,String lastName,boolean active,List<String> roles){}
package com.masterlearning.platform.modules.user.controller;

import com.masterlearning.platform.common.api.ApiResponse;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserRolesRequest;
import com.masterlearning.platform.modules.user.dto.request.UpdateUserStatusRequest;
import com.masterlearning.platform.modules.user.dto.response.UserResponse;
import com.masterlearning.platform.modules.user.service.UserManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserManagementControllerTest {

    @Mock
    private UserManagementService service;

    @InjectMocks
    private UserManagementController controller;

    @Test
    void getAllPassesQueryAndEnabledFiltersToService() {
        UserResponse user = new UserResponse(
                UUID.randomUUID(), "teacher@example.com", "Mira", "Patel",
                true, Set.of("TEACHER"));
        when(service.getAll("mira", true)).thenReturn(List.of(user));

        ApiResponse<List<UserResponse>> response = controller.getAll("mira", true);

        assertTrue(response.success());
        assertEquals("Users retrieved successfully", response.message());
        assertEquals(List.of(user), response.data());
        verify(service).getAll("mira", true);
        verifyNoMoreInteractions(service);
    }

    @Test
    void getAllAllowsMissingOptionalFilters() {
        when(service.getAll(null, null)).thenReturn(List.of());

        ApiResponse<List<UserResponse>> response = controller.getAll(null, null);

        assertTrue(response.success());
        assertTrue(response.data().isEmpty());
        verify(service).getAll(null, null);
        verifyNoMoreInteractions(service);
    }

    @Test
    void getByIdReturnsSuccessfulApiResponse() {
        UUID id = UUID.randomUUID();
        UserResponse user = new UserResponse(
                id, "teacher@example.com", "Mira", "Patel", true, Set.of("TEACHER"));
        when(service.getById(id)).thenReturn(user);

        ApiResponse<UserResponse> response = controller.getById(id);

        assertTrue(response.success());
        assertEquals("User retrieved successfully", response.message());
        assertEquals(user, response.data());
        verify(service).getById(id);
        verifyNoMoreInteractions(service);
    }

    @Test
    void updateStatusDelegatesRequestAndReturnsSuccessfulResponse() {
        UUID id = UUID.randomUUID();
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(false);
        UserResponse user = new UserResponse(
                id, "teacher@example.com", "Mira", "Patel", false, Set.of("TEACHER"));
        when(service.updateStatus(id, request)).thenReturn(user);

        ApiResponse<UserResponse> response = controller.updateStatus(id, request);

        assertTrue(response.success());
        assertEquals("User status updated successfully", response.message());
        assertEquals(user, response.data());
        verify(service).updateStatus(id, request);
        verifyNoMoreInteractions(service);
    }

    @Test
    void updateRolesDelegatesRequestAndReturnsSuccessfulResponse() {
        UUID id = UUID.randomUUID();
        UpdateUserRolesRequest request = new UpdateUserRolesRequest(Set.of("TEACHER"));
        UserResponse user = new UserResponse(
                id, "teacher@example.com", "Mira", "Patel", true, Set.of("TEACHER"));
        when(service.updateRoles(id, request)).thenReturn(user);

        ApiResponse<UserResponse> response = controller.updateRoles(id, request);

        assertTrue(response.success());
        assertEquals("User roles updated successfully", response.message());
        assertEquals(user, response.data());
        verify(service).updateRoles(id, request);
        verifyNoMoreInteractions(service);
    }

}

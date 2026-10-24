package com.masterlearning.platform.modules.user.controller;

import com.masterlearning.platform.common.api.ApiResponse;
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
}

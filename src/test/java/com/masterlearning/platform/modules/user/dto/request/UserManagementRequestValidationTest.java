package com.masterlearning.platform.modules.user.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserManagementRequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void rejectsNullEnabledStatus() {
        assertFalse(validator.validate(new UpdateUserStatusRequest(null)).isEmpty());
    }

    @Test
    void acceptsExplicitEnabledStatus() {
        assertTrue(validator.validate(new UpdateUserStatusRequest(true)).isEmpty());
        assertTrue(validator.validate(new UpdateUserStatusRequest(false)).isEmpty());
    }

    @Test
    void rejectsNullRoleSet() {
        assertFalse(validator.validate(new UpdateUserRolesRequest(null)).isEmpty());
    }

    @Test
    void rejectsEmptyRoleSet() {
        assertFalse(validator.validate(new UpdateUserRolesRequest(Set.of())).isEmpty());
    }

    @Test
    void acceptsNonEmptyRoleSet() {
        assertTrue(validator.validate(new UpdateUserRolesRequest(Set.of("TEACHER"))).isEmpty());
    }
}

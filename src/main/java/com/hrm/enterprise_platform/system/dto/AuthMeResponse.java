package com.hrm.enterprise_platform.system.dto;

import java.util.Set;

public record AuthMeResponse(
        Long id,
        String username,
        String fullName,
        String email,
        Set<String> roles
) {}

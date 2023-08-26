package com.itm.space.backendresources.api.response;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class UserResponse {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final List<String> roles;
    private final List<String> groups;

    public void setId(UUID userId) {
    }
}

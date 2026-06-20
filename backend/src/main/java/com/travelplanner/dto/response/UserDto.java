package com.travelplanner.dto.response;

import com.travelplanner.model.User;

public class UserDto {
    private String id;
    private String email;
    private String name;

    public static UserDto fromModel(User user) {
        UserDto dto = new UserDto();
        dto.id = user.getId();
        dto.email = user.getEmail();
        dto.name = user.getName();
        return dto;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}

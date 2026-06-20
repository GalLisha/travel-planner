package com.travelplanner.dto.response;

import com.travelplanner.service.AuthService;

public class AuthResponseDto {
    private String token;
    private UserDto user;

    public static AuthResponseDto from(AuthService.AuthResult result) {
        AuthResponseDto dto = new AuthResponseDto();
        dto.token = result.getToken();
        dto.user = UserDto.fromModel(result.getUser());
        return dto;
    }

    public String getToken() {
        return token;
    }

    public UserDto getUser() {
        return user;
    }
}

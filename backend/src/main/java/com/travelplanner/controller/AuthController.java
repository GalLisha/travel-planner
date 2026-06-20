package com.travelplanner.controller;

import com.travelplanner.dto.request.SigninRequestDto;
import com.travelplanner.dto.request.SignupRequestDto;
import com.travelplanner.dto.response.AuthResponseDto;
import com.travelplanner.http.RequestContext;
import com.travelplanner.http.Router;
import com.travelplanner.service.AuthService;

import java.io.IOException;

public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void registerRoutes(Router router) {
        router.post("/api/auth/signup", this::signup);
        router.post("/api/auth/signin", this::signin);
    }

    private void signup(RequestContext ctx) throws IOException {
        SignupRequestDto request = ctx.bodyAs(SignupRequestDto.class);
        AuthService.AuthResult result = authService.signup(request);
        ctx.sendJson(201, AuthResponseDto.from(result));
    }

    private void signin(RequestContext ctx) throws IOException {
        SigninRequestDto request = ctx.bodyAs(SigninRequestDto.class);
        AuthService.AuthResult result = authService.signin(request);
        ctx.sendJson(200, AuthResponseDto.from(result));
    }
}

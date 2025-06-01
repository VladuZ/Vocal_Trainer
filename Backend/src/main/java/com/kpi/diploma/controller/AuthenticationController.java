package com.kpi.diploma.controller;

import com.kpi.diploma.model.LoginRequest;
import com.kpi.diploma.model.LoginResponse;
import com.kpi.diploma.model.RegistrationRequest;
import com.kpi.diploma.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthenticationController {
    private final UserService userService;

    public AuthenticationController (UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register (@RequestBody RegistrationRequest reg) {
        LoginResponse loginResponse = userService.register(reg);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login (@RequestBody LoginRequest log) {
        LoginResponse loginResponse = userService.login(log);
        return ResponseEntity.ok(loginResponse);
    }
}

package com.ruiz.api.service;

import com.ruiz.api.dto.AuthResponse;
import com.ruiz.api.dto.LoginRequest;
import com.ruiz.api.dto.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}

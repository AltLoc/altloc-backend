package com.altloc.backend.api.auth.password;

import com.altloc.backend.model.LoginDTO;
import com.altloc.backend.model.LoginResponse;
import com.altloc.backend.model.RegistrationDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/registration")
    public ResponseEntity<?> registration(@RequestBody RegistrationDTO request) {
        return passwordService.registerUser(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginDTO requestLogin, HttpServletResponse response) {
        return passwordService.loginUser(requestLogin, response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        return passwordService.refreshToken(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        return passwordService.logoutUser(response);
    }
}

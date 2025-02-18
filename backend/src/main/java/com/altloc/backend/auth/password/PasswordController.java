package com.altloc.backend.auth.password;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.altloc.backend.exception.UserAlreadyExistException;
import com.altloc.backend.model.RegistrationDTO;

@RestController
@RequestMapping("/auth/password")
public class PasswordController {

    @Autowired
    private PasswordService passwordService;

    @PostMapping("/registration")
    public ResponseEntity<String> registration(@Valid @RequestBody RegistrationDTO request) {
        try {
            passwordService.registration(request);
            return ResponseEntity.ok("User successfully registered");
        } catch (UserAlreadyExistException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something went wrong");
        }
    }
}

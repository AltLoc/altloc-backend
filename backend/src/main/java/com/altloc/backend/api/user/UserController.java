package com.altloc.backend.api.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

import com.altloc.backend.api.user.dto.UserDto;
import com.altloc.backend.api.user.factories.UserDtoFactory;
import com.altloc.backend.model.UserDetailsImpl;
import com.altloc.backend.store.entities.UserEntity;

import lombok.RequiredArgsConstructor;

import java.security.Principal;

@RequiredArgsConstructor

@RestController
@RequestMapping("/secured")
public class UserController {

    private final UserDtoFactory userDtoFactory;

    @GetMapping("/user")
    public String userAccess(Principal principal) {
        if (principal == null)
            return null;

        return principal.getName();

    }

    @GetMapping("/me")
    public ResponseEntity<UserDetailsImpl> authenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/me2")
    public ResponseEntity<UserDto> currentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity user = (UserEntity) authentication.getPrincipal();
            return ResponseEntity.ok(userDtoFactory.createUserDto(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}

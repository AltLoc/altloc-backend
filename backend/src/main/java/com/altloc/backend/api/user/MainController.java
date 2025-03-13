package com.altloc.backend.api.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.altloc.backend.model.UserDetailsImpl;

// import com.altloc.backend.store.entity.UserEntity;

// import java.security.Principal;

import org.springframework.security.core.Authentication;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/secured")
public class MainController {

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

}

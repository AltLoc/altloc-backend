package com.altloc.backend.api.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

import com.altloc.backend.model.UserDetailsImpl;

import java.security.Principal;

@RestController
@RequestMapping("/secured")
public class UserController {

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

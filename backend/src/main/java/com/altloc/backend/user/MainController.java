package com.altloc.backend.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

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

}

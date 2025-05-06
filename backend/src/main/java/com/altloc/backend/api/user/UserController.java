package com.altloc.backend.api.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.security.core.Authentication;

import com.altloc.backend.api.user.dto.UserDto;
import com.altloc.backend.api.user.factories.UserDtoFactory;
import com.altloc.backend.model.UserDetailsImpl;
import com.altloc.backend.service.MinioService;
import com.altloc.backend.store.entities.UserEntity;
import com.altloc.backend.store.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/secured")
public class UserController {

    private final UserDtoFactory userDtoFactory;
    private final UserRepository userRepository;
    private final MinioService minioService;

    public static final String UPDATE_USER = "/user/profile";

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

    @PutMapping(value = UPDATE_USER, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDto> updateUserProfile(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Optional<UserEntity> optionalUser = userRepository.findById(userDetails.getId());
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserEntity user = optionalUser.get();

        if (username != null && !username.trim().isEmpty()) {
            user.setUsername(username);
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String avatarKey = "users/avatar/" + UUID.randomUUID() + "-" + avatarFile.getOriginalFilename();
                minioService.uploadFile(avatarKey, avatarFile.getInputStream(), avatarFile.getSize(),
                        avatarFile.getContentType());
                user.setAvatarKey(avatarKey);
            } catch (Exception e) {
                throw new RuntimeException("Error loading avatar in MinIO: " + e.getMessage(), e);
            }
        }

        userRepository.saveAndFlush(user);

        return ResponseEntity.ok(userDtoFactory.createUserDto(user));
    }

}
